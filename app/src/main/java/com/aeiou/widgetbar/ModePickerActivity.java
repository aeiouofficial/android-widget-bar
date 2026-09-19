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

public final class ModePickerActivity extends Activity {
    static final String EXTRA_PROVIDER_ID = "provider_id";
    static final String EXTRA_ANCHOR_SIDE = "anchor_side";
    static final String EXTRA_FROM_WHEEL = "from_wheel";
    static final String ANCHOR_LEFT = "left";
    static final String ANCHOR_RIGHT = "right";

    private static final int ITEM_SIZE_DP = 48;
    private static final int GAP_DP = 6;

    private final Map<ProviderMode, ImageButton> modeButtons =
            new EnumMap<>(ProviderMode.class);

    private ProviderTarget provider;
    private ProviderMode selected;
    private LinearLayout modes;
    private String anchorSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        configureWindow();

        String requestedProvider = getIntent().getStringExtra(EXTRA_PROVIDER_ID);
        provider = requestedProvider == null
                ? WidgetPrefs.getProvider(this)
                : ProviderTarget.fromId(requestedProvider);
        anchorSide = getIntent().getStringExtra(EXTRA_ANCHOR_SIDE);
        if (anchorSide == null) {
            anchorSide = ANCHOR_LEFT;
        }

        if (requestedProvider != null) {
            WidgetPrefs.setProvider(this, provider);
            WidgetUpdates.updateAll(this);
        }

        selected = WidgetPrefs.getMode(this, provider);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setOnClickListener(v -> finish());

        modes = buildModes();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        root.addView(modes, lp);

        setContentView(root);
        modes.post(this::positionModes);
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

    private LinearLayout buildModes() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        column.setPadding(dp(6), dp(6), dp(6), dp(6));
        column.setBackground(rounded(0xF0222D31, 28, 0x806C858C, 1));
        column.setOnClickListener(v -> { });

        ProviderMode[] providerModes = ProviderMode.modesFor(provider);
        for (int i = 0; i < providerModes.length; i++) {
            ProviderMode mode = providerModes[i];

            ImageButton button = new ImageButton(this);
            button.setImageResource(mode.iconRes);
            button.setContentDescription(provider.label + ": " + mode.label);
            button.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            button.setPadding(dp(9), dp(9), dp(9), dp(9));
            button.setOnClickListener(v -> select(mode));
            modeButtons.put(mode, button);

            LinearLayout.LayoutParams itemLp =
                    new LinearLayout.LayoutParams(dp(ITEM_SIZE_DP), dp(ITEM_SIZE_DP));
            if (i > 0) itemLp.topMargin = dp(GAP_DP);
            column.addView(button, itemLp);
        }

        refreshSelection();
        return column;
    }

    private void select(ProviderMode mode) {
        selected = mode;
        WidgetPrefs.setProvider(this, provider);
        WidgetPrefs.setMode(this, mode);
        WidgetUpdates.updateAll(this);

        if (!mode.acceptsText) {
            SearchLauncher.launchAction(this, mode);
        }

        finish();
    }

    private void refreshSelection() {
        for (Map.Entry<ProviderMode, ImageButton> entry : modeButtons.entrySet()) {
            boolean active = entry.getKey() == selected;
            entry.getValue().setBackground(rounded(
                    active ? 0xCC314348 : 0xB01D292D,
                    24,
                    active ? 0xFF61E3EA : 0x806A8990,
                    active ? 2 : 1));
        }
    }

    private void positionModes() {
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) modes.getLayoutParams();

        Rect source = getIntent().getSourceBounds();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int edgeMargin = dp(10);
        int gap = dp(8);
        boolean fromWheel = getIntent().getBooleanExtra(EXTRA_FROM_WHEEL, false);

        int anchorCenterX;
        int anchorTopY;

        if (fromWheel) {
            // Collection-widget source bounds are launcher-dependent. The wheel
            // is always on the right, so keep its mode menu right-anchored.
            anchorCenterX = width - dp(46);
            anchorTopY = source != null && !source.isEmpty()
                    ? source.top
                    : height - dp(240);
        } else if (source != null && !source.isEmpty()) {
            anchorCenterX = source.centerX();
            anchorTopY = source.top;
        } else {
            anchorCenterX = ANCHOR_RIGHT.equals(anchorSide)
                    ? width - dp(46)
                    : dp(46);
            anchorTopY = height - dp(240);
        }

        int desiredLeft = anchorCenterX - modes.getWidth() / 2;
        int desiredTop = anchorTopY - modes.getHeight() - gap;

        lp.leftMargin = clamp(
                desiredLeft,
                edgeMargin,
                width - modes.getWidth() - edgeMargin);
        lp.topMargin = clamp(
                desiredTop,
                edgeMargin,
                height - modes.getHeight() - edgeMargin);

        modes.setLayoutParams(lp);
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
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
