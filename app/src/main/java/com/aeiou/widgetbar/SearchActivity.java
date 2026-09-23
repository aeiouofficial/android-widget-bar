package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public final class SearchActivity extends Activity {
    static final String EXTRA_WIDGET_DOUBLE_TAP = "widget_double_tap";
    static final String EXTRA_WIDGET_VARIANT = "widget_variant";

    private static final int BAR_HEIGHT_DP = 48;
    private static final int EDGE_MARGIN_DP = 15;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ProviderTarget selected;
    private ProviderMode selectedMode;
    private String widgetVariant;
    private FrameLayout root;
    private LinearLayout searchBar;
    private EditText searchField;
    private ImageView activeIcon;
    private ImageButton classicSelectorButton;
    private ListView wheelList;
    private Rect sourceBounds;

    private boolean gestureAware;
    private boolean waitingForSecondTap;
    private boolean editingActive;
    private long firstTapAt = -1L;
    private int doubleTapTimeout;
    private Runnable beginEditingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        selected = WidgetPrefs.getProvider(this);
        selectedMode = WidgetPrefs.getMode(this, selected);
        sourceBounds = getIntent().getSourceBounds();
        gestureAware = getIntent().getBooleanExtra(EXTRA_WIDGET_DOUBLE_TAP, false);
        widgetVariant = getIntent().getStringExtra(EXTRA_WIDGET_VARIANT);
        if (widgetVariant == null) {
            widgetVariant = WidgetUpdates.VARIANT_CLASSIC;
        }
        configureWindow();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setOnClickListener(v -> {
            if (editingActive) finish();
        });

        searchBar = buildSearchBar();
        searchBar.setVisibility(gestureAware ? View.INVISIBLE : View.VISIBLE);

        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(BAR_HEIGHT_DP));
        barLp.gravity = Gravity.TOP | Gravity.START;
        root.addView(searchBar, barLp);

        setContentView(root);

        root.addOnLayoutChangeListener((v, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom) ->
                positionSearchBar());
        root.getViewTreeObserver().addOnGlobalLayoutListener(this::positionSearchBar);
        searchBar.post(this::positionSearchBar);

        if (gestureAware) {
            armDoubleTapWindow();
        } else {
            performSingleTapAction();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String incomingVariant = intent.getStringExtra(EXTRA_WIDGET_VARIANT);
        if (incomingVariant != null) {
            widgetVariant = incomingVariant;
        }

        boolean incomingGestureAware =
                intent.getBooleanExtra(EXTRA_WIDGET_DOUBLE_TAP, false);
        if (!incomingGestureAware) return;

        long now = SystemClock.elapsedRealtime();
        if (waitingForSecondTap
                && TapGesturePolicy.isDoubleTap(firstTapAt, now, doubleTapTimeout)) {
            waitingForSecondTap = false;
            cancelBeginEditing();
            clearTapPassthrough();
            SearchLauncher.openAppHome(this, selected);
            finish();
            return;
        }

        gestureAware = true;
        armDoubleTapWindow();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && waitingForSecondTap) {
            clearTapPassthrough();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (waitingForSecondTap && event.getActionMasked() == MotionEvent.ACTION_UP) {
            long now = SystemClock.elapsedRealtime();
            if (TapGesturePolicy.isDoubleTap(firstTapAt, now, doubleTapTimeout)) {
                waitingForSecondTap = false;
                cancelBeginEditing();
                SearchLauncher.openAppHome(this, selected);
                finish();
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gestureAware && !editingActive && selectedMode.acceptsText) {
            activateEditing();
        }
    }

    @Override
    protected void onPause() {
        cancelBeginEditing();
        clearTapPassthrough();
        if (editingActive) {
            WidgetUpdates.setEditing(this, widgetVariant, false);
            editingActive = false;
        }
        super.onPause();
    }

    @Override
    public void finish() {
        cancelBeginEditing();
        clearTapPassthrough();
        if (editingActive) {
            WidgetUpdates.setEditing(this, widgetVariant, false);
            editingActive = false;
        }
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void armDoubleTapWindow() {
        cancelBeginEditing();
        clearTapPassthrough();

        firstTapAt = SystemClock.elapsedRealtime();
        doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
        waitingForSecondTap = true;
        searchBar.setVisibility(View.INVISIBLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        WidgetUpdates.setEditing(this, widgetVariant, false);

        beginEditingRunnable = () -> {
            if (!waitingForSecondTap) return;
            waitingForSecondTap = false;
            clearTapPassthrough();
            performSingleTapAction();
        };
        handler.postDelayed(beginEditingRunnable, doubleTapTimeout);
    }

    private void performSingleTapAction() {
        beginEditingRunnable = null;

        if (!selectedMode.acceptsText) {
            SearchLauncher.launchAction(this, selectedMode);
            finish();
            return;
        }

        activateEditing();
        beginEditingSoon(80);
    }

    private void activateEditing() {
        if (editingActive) return;
        editingActive = true;
        searchBar.setVisibility(View.VISIBLE);
        WidgetUpdates.setEditing(this, widgetVariant, true);
    }

    private void clearTapPassthrough() {
        Window window = getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void beginEditingSoon(long delayMs) {
        cancelBeginEditing();
        beginEditingRunnable = this::beginEditing;
        handler.postDelayed(beginEditingRunnable, delayMs);
    }

    private void beginEditing() {
        beginEditingRunnable = null;
        if (!selectedMode.acceptsText) return;

        searchField.requestFocus();
        searchField.post(() -> {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void cancelBeginEditing() {
        if (beginEditingRunnable != null) {
            handler.removeCallbacks(beginEditingRunnable);
            beginEditingRunnable = null;
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private LinearLayout buildSearchBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), 0, dp(8), 0);
        row.setBackground(rounded(0xC2222D31, 34, 0x4D6C858C, 1));
        row.setOnClickListener(v -> { });

        activeIcon = new ImageView(this);
        activeIcon.setContentDescription(selected.label + " modes");
        activeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        activeIcon.setPadding(dp(6), dp(6), dp(6), dp(6));
        activeIcon.setOnClickListener(v -> openModePickerLeft());
        row.addView(activeIcon, new LinearLayout.LayoutParams(dp(40), dp(40)));

        searchField = new EditText(this);
        searchField.setSingleLine(true);
        searchField.setTextColor(Color.WHITE);
        searchField.setHintTextColor(0xFFB9C9CC);
        searchField.setTextSize(18f);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchField.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        searchField.setPadding(dp(8), 0, dp(8), 0);
        searchField.setBackgroundColor(Color.TRANSPARENT);
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                submit();
                return true;
            }
            return false;
        });
        row.addView(
                searchField,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f));

        if (WidgetUpdates.VARIANT_WHEEL.equals(widgetVariant)) {
            wheelList = new ListView(this);
            wheelList.setContentDescription(getString(R.string.wheel_provider_button));
            wheelList.setDivider(null);
            wheelList.setDividerHeight(0);
            wheelList.setVerticalScrollBarEnabled(false);
            wheelList.setOverScrollMode(View.OVER_SCROLL_NEVER);
            wheelList.setBackgroundColor(Color.TRANSPARENT);
            wheelList.setAdapter(new ProviderRailAdapter());
            wheelList.setSelection(
                    ProviderWheelPolicy.centeredPositionForProvider(
                            selected.ordinal(),
                            ProviderTarget.values().length));
            wheelList.setOnItemClickListener((parent, view, position, id) -> {
                ProviderTarget[] providers = ProviderTarget.values();
                if (providers.length == 0) return;

                selected = providers[
                        ProviderWheelPolicy.providerIndexForPosition(
                                position,
                                providers.length)];
                selectedMode = WidgetPrefs.getMode(this, selected);
                WidgetPrefs.setProvider(this, selected);
                WidgetUpdates.updateAll(this);
                refreshProviderUi();
                openModePickerRight();
            });
            row.addView(
                    wheelList,
                    new LinearLayout.LayoutParams(dp(40), dp(40)));
        } else {
            classicSelectorButton = new ImageButton(this);
            classicSelectorButton.setContentDescription(getString(R.string.provider_button));
            classicSelectorButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            classicSelectorButton.setPadding(dp(9), dp(9), dp(9), dp(9));
            classicSelectorButton.setBackgroundColor(Color.TRANSPARENT);
            classicSelectorButton.setImageResource(R.drawable.ic_provider_picker);
            classicSelectorButton.setOnClickListener(v -> openSelector());
            row.addView(
                    classicSelectorButton,
                    new LinearLayout.LayoutParams(dp(40), dp(40)));
        }

        refreshProviderUi();
        return row;
    }

    private void refreshProviderUi() {
        if (activeIcon == null || searchField == null) return;

        activeIcon.setContentDescription(selected.label + " modes");
        activeIcon.setImageBitmap(IconLoader.load(
                this,
                selected.packageName,
                dp(40),
                selected.label.substring(0, 1)));

        searchField.setHint(selectedMode.inputHint);
        searchField.setEnabled(selectedMode.acceptsText);

        if (wheelList != null) {
            wheelList.setContentDescription(
                    selected.label + " infinite reel; swipe vertically and tap an app for modes");
            wheelList.setSelection(
                    ProviderWheelPolicy.centeredPositionForProvider(
                            selected.ordinal(),
                            ProviderTarget.values().length));
        }
    }

    private void openModePickerLeft() {
        hideKeyboard();

        Intent modes = new Intent(this, ModePickerActivity.class)
                .putExtra(ModePickerActivity.EXTRA_PROVIDER_ID, selected.id)
                .putExtra(ModePickerActivity.EXTRA_ANCHOR_SIDE, ModePickerActivity.ANCHOR_LEFT);
        setLeftSourceBounds(modes);
        startActivity(modes);
        finish();
    }

    private void openModePickerRight() {
        hideKeyboard();

        Intent modes = new Intent(this, ModePickerActivity.class)
                .putExtra(ModePickerActivity.EXTRA_PROVIDER_ID, selected.id)
                .putExtra(ModePickerActivity.EXTRA_ANCHOR_SIDE, ModePickerActivity.ANCHOR_RIGHT);
        setRightSourceBounds(modes);
        startActivity(modes);
        finish();
    }

    private void openSelector() {
        hideKeyboard();

        Intent selector = new Intent(this, PickerActivity.class);
        setRightSourceBounds(selector);
        startActivity(selector);
        finish();
    }

    private void setLeftSourceBounds(Intent intent) {
        if (sourceBounds != null && !sourceBounds.isEmpty()) {
            int iconWidth = dp(40);
            intent.setSourceBounds(new Rect(
                    sourceBounds.left,
                    sourceBounds.top,
                    Math.min(sourceBounds.right, sourceBounds.left + iconWidth),
                    sourceBounds.bottom));
            return;
        }

        int[] location = new int[2];
        activeIcon.getLocationOnScreen(location);
        intent.setSourceBounds(new Rect(
                location[0],
                location[1],
                location[0] + activeIcon.getWidth(),
                location[1] + activeIcon.getHeight()));
    }

    private void setRightSourceBounds(Intent intent) {
        if (sourceBounds != null && !sourceBounds.isEmpty()) {
            int selectorWidth = dp(40);
            intent.setSourceBounds(new Rect(
                    Math.max(sourceBounds.left, sourceBounds.right - selectorWidth),
                    sourceBounds.top,
                    sourceBounds.right,
                    sourceBounds.bottom));
            return;
        }

        View anchor = wheelList != null ? wheelList : classicSelectorButton;
        if (anchor == null) return;

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        intent.setSourceBounds(new Rect(
                location[0],
                location[1],
                location[0] + anchor.getWidth(),
                location[1] + anchor.getHeight()));
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
        }
    }

    private void submit() {
        if (SearchLauncher.launch(this, selectedMode, searchField.getText().toString())) {
            finish();
        }
    }

    private void positionSearchBar() {
        if (root == null || searchBar == null || searchBar.getHeight() == 0) return;

        Rect visible = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int margin = dp(EDGE_MARGIN_DP);
        int desiredLeft = margin;
        int desiredWidth = screenWidth - margin * 2;

        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        lp.width = desiredWidth;
        lp.height = dp(BAR_HEIGHT_DP);
        lp.leftMargin = clamp(
                desiredLeft - visible.left,
                margin,
                Math.max(margin, root.getWidth() - desiredWidth - margin));

        int sourceTop = sourceBounds != null && !sourceBounds.isEmpty()
                ? sourceBounds.top
                : visible.bottom - lp.height - margin;
        int sourceCenteredTop = sourceBounds != null && !sourceBounds.isEmpty()
                ? sourceTop + Math.max(0, (sourceBounds.height() - lp.height) / 2)
                : sourceTop;

        int keyboardSafeTop = visible.bottom - lp.height - margin;
        int desiredScreenTop = Math.min(sourceCenteredTop, keyboardSafeTop);
        int rootTopOnScreen = visible.top;
        lp.topMargin = Math.max(margin, desiredScreenTop - rootTopOnScreen);

        searchBar.setLayoutParams(lp);
    }

    private int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private GradientDrawable rounded(
            int color,
            int radiusDp,
            int strokeColor,
            int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class ProviderRailAdapter extends BaseAdapter {
        private final ProviderTarget[] providers = ProviderTarget.values();

        @Override
        public int getCount() {
            return ProviderWheelPolicy.VIRTUAL_ITEM_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return providers[
                    ProviderWheelPolicy.providerIndexForPosition(
                            position,
                            providers.length)];
        }

        @Override
        public long getItemId(int position) {
            return ProviderWheelPolicy.providerIndexForPosition(
                    position,
                    providers.length);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView icon = convertView instanceof ImageView
                    ? (ImageView) convertView
                    : new ImageView(SearchActivity.this);

            ProviderTarget target = providers[
                    ProviderWheelPolicy.providerIndexForPosition(
                            position,
                            providers.length)];
            icon.setLayoutParams(new ListView.LayoutParams(dp(40), dp(32)));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setPadding(dp(5), dp(3), dp(5), dp(3));
            icon.setBackgroundColor(Color.TRANSPARENT);
            icon.setContentDescription(target.label);
            icon.setImageBitmap(IconLoader.load(
                    SearchActivity.this,
                    target.packageName,
                    dp(32),
                    target.label.substring(0, 1)));
            return icon;
        }
    }
}
