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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public final class SearchActivity extends Activity {
    static final String EXTRA_WIDGET_DOUBLE_TAP = "widget_double_tap";

    private static final int BAR_HEIGHT_DP = 48;
    private static final int EDGE_MARGIN_DP = 15;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private ProviderTarget selected;
    private ProviderMode selectedMode;
    private FrameLayout root;
    private LinearLayout searchBar;
    private EditText searchField;
    private ImageView activeIcon;
    private ImageButton selectorButton;
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
            SearchBarWidgetProvider.setEditing(this, false);
            editingActive = false;
        }
        super.onPause();
    }

    @Override
    public void finish() {
        cancelBeginEditing();
        clearTapPassthrough();
        if (editingActive) {
            SearchBarWidgetProvider.setEditing(this, false);
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

        /*
         * Very early second taps must still reach the launcher widget.
         * Once this transparent window gains focus, onWindowFocusChanged()
         * clears NOT_TOUCHABLE and later second taps are captured directly.
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        SearchBarWidgetProvider.setEditing(this, false);

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
        SearchBarWidgetProvider.setEditing(this, true);
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
        activeIcon.setImageBitmap(IconLoader.load(
                this,
                selected.packageName,
                dp(40),
                selected.label.substring(0, 1)));
        activeIcon.setOnClickListener(v -> openModePicker());
        row.addView(activeIcon, new LinearLayout.LayoutParams(dp(40), dp(40)));

        searchField = new EditText(this);
        searchField.setSingleLine(true);
        searchField.setTextColor(Color.WHITE);
        searchField.setHintTextColor(0xFFB9C9CC);
        searchField.setTextSize(18f);
        searchField.setHint(selectedMode.inputHint);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchField.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        searchField.setPadding(dp(8), 0, dp(8), 0);
        searchField.setBackgroundColor(Color.TRANSPARENT);
        searchField.setEnabled(selectedMode.acceptsText);
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

        selectorButton = new ImageButton(this);
        selectorButton.setContentDescription(getString(R.string.provider_button));
        selectorButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        selectorButton.setPadding(dp(9), dp(9), dp(9), dp(9));
        selectorButton.setBackgroundColor(Color.TRANSPARENT);
        selectorButton.setImageResource(R.drawable.ic_provider_picker);
        selectorButton.setOnClickListener(v -> openSelector());
        row.addView(
                selectorButton,
                new LinearLayout.LayoutParams(dp(40), dp(40)));

        return row;
    }

    private void openModePicker() {
        hideKeyboard();

        Intent modes = new Intent(this, ModePickerActivity.class);
        if (sourceBounds != null && !sourceBounds.isEmpty()) {
            int iconWidth = dp(40);
            modes.setSourceBounds(new Rect(
                    sourceBounds.left,
                    sourceBounds.top,
                    Math.min(sourceBounds.right, sourceBounds.left + iconWidth),
                    sourceBounds.bottom));
        } else {
            int[] location = new int[2];
            activeIcon.getLocationOnScreen(location);
            modes.setSourceBounds(new Rect(
                    location[0],
                    location[1],
                    location[0] + activeIcon.getWidth(),
                    location[1] + activeIcon.getHeight()));
        }

        startActivity(modes);
        finish();
    }

    private void openSelector() {
        hideKeyboard();

        Intent selector = new Intent(this, PickerActivity.class);
        if (sourceBounds != null && !sourceBounds.isEmpty()) {
            int selectorWidth = dp(40);
            selector.setSourceBounds(new Rect(
                    Math.max(sourceBounds.left, sourceBounds.right - selectorWidth),
                    sourceBounds.top,
                    sourceBounds.right,
                    sourceBounds.bottom));
        }
        startActivity(selector);
        finish();
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
}
