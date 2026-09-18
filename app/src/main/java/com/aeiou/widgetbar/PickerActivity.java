package com.aeiou.widgetbar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.EnumMap;
import java.util.Map;

public final class PickerActivity extends Activity {
    private final Map<ProviderTarget, ImageButton> providerButtons = new EnumMap<>(ProviderTarget.class);
    private ProviderTarget selected;
    private LinearLayout selector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        selected = WidgetPrefs.getProvider(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setOnClickListener(v -> finish());

        selector = buildSelector();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        root.addView(selector, lp);

        setContentView(root);
        selector.post(this::positionSelector);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private LinearLayout buildSelector() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(8), dp(6), dp(8), dp(6));
        row.setBackground(rounded(0xF0222D31, 28, 0x806C858C, 1));
        row.setOnClickListener(v -> { });

        for (ProviderTarget target : ProviderTarget.values()) {
            ImageButton button = new ImageButton(this);
            button.setContentDescription(target.label);
            button.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            button.setPadding(dp(8), dp(8), dp(8), dp(8));
            button.setImageBitmap(IconLoader.load(
                    this, target.packageName, dp(44), target.label.substring(0, 1)));
            button.setOnClickListener(v -> select(target));
            providerButtons.put(target, button);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
            lp.setMargins(dp(3), 0, dp(3), 0);
            row.addView(button, lp);
        }

        refreshSelection();
        return row;
    }

    private void select(ProviderTarget target) {
        selected = target;
        WidgetPrefs.setProvider(this, target);
        SearchBarWidgetProvider.updateAll(this);
        finish();
    }

    private void refreshSelection() {
        for (Map.Entry<ProviderTarget, ImageButton> entry : providerButtons.entrySet()) {
            boolean active = entry.getKey() == selected;
            entry.getValue().setBackground(rounded(
                    active ? 0xCC314348 : Color.TRANSPARENT,
                    24,
                    active ? 0xFF61E3EA : 0x006C858C,
                    active ? 2 : 0));
        }
    }

    private void positionSelector() {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) selector.getLayoutParams();
        Rect source = getIntent().getSourceBounds();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int margin = dp(10);

        int right = source != null && !source.isEmpty()
                ? source.right
                : width - margin;
        int top;
        if (source != null && !source.isEmpty()) {
            top = source.top - selector.getHeight() - dp(8);
        } else {
            // Launcher3 does not propagate sourceBounds for RemoteViews PendingIntents.
            // Keep the icon-only selector safely on-screen in the same lower-home area
            // where a search-bar widget normally lives instead of using a dp value that
            // can place it below the physical display on high-density phones.
            int anchorBottom = Math.round(height * 0.43f);
            top = anchorBottom - selector.getHeight();
        }

        lp.leftMargin = clamp(
                right - selector.getWidth(),
                margin,
                width - selector.getWidth() - margin);
        lp.topMargin = clamp(
                top,
                margin,
                height - selector.getHeight() - margin);
        selector.setLayoutParams(lp);
    }

    private int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
