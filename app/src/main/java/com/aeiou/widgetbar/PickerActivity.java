package com.aeiou.widgetbar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.EnumMap;
import java.util.Map;

public final class PickerActivity extends Activity {
    private final Map<ProviderTarget, ImageButton> providerButtons = new EnumMap<>(ProviderTarget.class);
    private ProviderTarget selected;
    private EditText searchField;
    private LinearLayout card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        selected = WidgetPrefs.getProvider(this);
        setContentView(buildContent());
        positionCard();

        if (getIntent().getBooleanExtra(SearchBarWidgetProvider.EXTRA_FOCUS_SEARCH, false)) {
            searchField.requestFocus();
            searchField.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
            }, 180);
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private View buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setOnClickListener(v -> finish());

        card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(rounded(0xEE26363B, 28, 0x806A8990, 1));
        card.setOnClickListener(v -> { });

        searchField = new EditText(this);
        searchField.setSingleLine(true);
        searchField.setTextColor(Color.WHITE);
        searchField.setHintTextColor(0xFFB9C9CC);
        searchField.setTextSize(17f);
        searchField.setHint(selected.hint);
        searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchField.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        searchField.setPadding(dp(16), 0, dp(16), 0);
        searchField.setBackground(rounded(0xB83E5055, 24, 0x406F8B91, 1));
        Drawable searchIcon = getDrawable(android.R.drawable.ic_menu_search);
        if (searchIcon != null) {
            searchIcon.setTint(0xFFD3E4E7);
            searchField.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
            searchField.setCompoundDrawablePadding(dp(10));
        }
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

        LinearLayout.LayoutParams fieldLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        card.addView(searchField, fieldLp);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(12), 0, 0);

        for (ProviderTarget target : ProviderTarget.values()) {
            ImageButton button = new ImageButton(this);
            button.setContentDescription(target.label);
            button.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            button.setPadding(dp(9), dp(9), dp(9), dp(9));
            button.setImageBitmap(IconLoader.load(
                    this, target.packageName, dp(48), target.label.substring(0, 1)));
            button.setOnClickListener(v -> select(target));
            providerButtons.put(target, button);

            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(56), dp(56));
            iconLp.setMargins(dp(5), 0, dp(5), 0);
            row.addView(button, iconLp);
        }

        card.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        cardLp.leftMargin = dp(16);
        cardLp.rightMargin = dp(16);
        cardLp.gravity = Gravity.TOP;
        root.addView(card, cardLp);

        refreshSelection();
        return root;
    }

    private void select(ProviderTarget target) {
        selected = target;
        WidgetPrefs.setProvider(this, target);
        SearchBarWidgetProvider.updateAll(this);
        searchField.setHint(target.hint);
        refreshSelection();
    }

    private void refreshSelection() {
        for (Map.Entry<ProviderTarget, ImageButton> entry : providerButtons.entrySet()) {
            boolean active = entry.getKey() == selected;
            entry.getValue().setBackground(rounded(
                    active ? 0x663EDDE8 : 0x44233135,
                    28,
                    active ? 0xFF61E3EA : 0x406D858B,
                    active ? 2 : 1));
        }
    }

    private void submit() {
        SearchLauncher.launch(this, selected, searchField.getText().toString());
        finish();
    }

    private void positionCard() {
        card.post(() -> {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) card.getLayoutParams();
            Rect source = getIntent().getSourceBounds();
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int desiredTop;
            if (source != null) {
                desiredTop = source.top - card.getHeight() - dp(10);
            } else {
                desiredTop = screenHeight - card.getHeight() - dp(160);
            }
            lp.topMargin = Math.max(dp(18), desiredTop);
            card.setLayoutParams(lp);
        });
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
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
