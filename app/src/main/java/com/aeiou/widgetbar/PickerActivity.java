package com.aeiou.widgetbar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.EnumMap;
import java.util.Map;

public final class PickerActivity extends Activity {
    private static final int ITEM_SIZE_DP = 48;
    private static final int GAP_DP = 6;

    private final Map<ProviderTarget, ImageButton> providerButtons =
            new EnumMap<>(ProviderTarget.class);

    private ProviderTarget selected;
    private LinearLayout selector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private LinearLayout buildSelector() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        column.setPadding(dp(6), dp(6), dp(6), dp(6));
        column.setBackground(rounded(0xF0222D31, 28, 0x806C858C, 1));
        column.setOnClickListener(v -> { });

        ProviderTarget[] targets = ProviderTarget.values();
        for (int i = 0; i < targets.length; i++) {
            ProviderTarget target = targets[i];

            ImageButton button = new ImageButton(this);
            button.setContentDescription(target.label);
            button.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            button.setPadding(dp(8), dp(8), dp(8), dp(8));
            button.setImageBitmap(IconLoader.load(
                    this,
                    target.packageName,
                    dp(44),
                    target.label.substring(0, 1)));
            button.setOnClickListener(v -> select(target));
            providerButtons.put(target, button);

            LinearLayout.LayoutParams itemLp =
                    new LinearLayout.LayoutParams(dp(ITEM_SIZE_DP), dp(ITEM_SIZE_DP));
            if (i > 0) {
                itemLp.topMargin = dp(GAP_DP);
            }
            column.addView(button, itemLp);
        }

        refreshSelection();
        return column;
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
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) selector.getLayoutParams();

        Rect source = getIntent().getSourceBounds();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int edgeMargin = dp(10);
        int gap = dp(8);

        int anchorCenterX;
        int anchorTopY;

        if (source != null && !source.isEmpty()) {
            anchorCenterX = source.centerX();
            anchorTopY = source.top;
        } else {
            /*
             * Launcher3 does not reliably propagate sourceBounds for RemoteViews
             * PendingIntents. Keep the selector on the right edge and above the
             * normal widget position instead of letting it overlap the widget.
             */
            anchorCenterX = width - dp(46);
            anchorTopY = height - dp(240);
        }

        int desiredLeft = anchorCenterX - selector.getWidth() / 2;
        int desiredTop = anchorTopY - selector.getHeight() - gap;

        lp.leftMargin = clamp(
                desiredLeft,
                edgeMargin,
                width - selector.getWidth() - edgeMargin);
        lp.topMargin = clamp(
                desiredTop,
                edgeMargin,
                height - selector.getHeight() - edgeMargin);

        selector.setLayoutParams(lp);
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
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
