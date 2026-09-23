package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.Intent;
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

public final class ChatGptActionsActivity extends Activity {
    private static final int ITEM_SIZE_DP = 48;
    private static final int GAP_DP = 6;

    private LinearLayout actions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        configureWindow();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.TRANSPARENT);
        root.setOnClickListener(v -> finish());

        actions = buildActions();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        root.addView(actions, lp);

        setContentView(root);
        actions.post(this::positionActions);
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

    private LinearLayout buildActions() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        column.setPadding(dp(6), dp(6), dp(6), dp(6));
        column.setBackground(rounded(0xF0222D31, 28, 0x806C858C, 1));
        column.setOnClickListener(v -> { });

        addAction(column, R.drawable.ic_voice, R.string.chatgpt_voice,
                ChatGptMediaActivity.ACTION_VOICE, 0);
        addAction(column, R.drawable.ic_camera, R.string.chatgpt_camera,
                ChatGptMediaActivity.ACTION_CAMERA, 1);
        addAction(column, R.drawable.ic_image, R.string.chatgpt_photo,
                ChatGptMediaActivity.ACTION_PHOTO, 2);
        addAction(column, R.drawable.ic_mic, R.string.chatgpt_dictation,
                ChatGptMediaActivity.ACTION_DICTATION, 3);

        return column;
    }

    private void addAction(
            LinearLayout parent,
            int iconRes,
            int descriptionRes,
            String action,
            int index) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(getString(descriptionRes));
        button.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        button.setPadding(dp(9), dp(9), dp(9), dp(9));
        button.setBackground(rounded(0xB01D292D, 24, 0x806A8990, 1));
        button.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatGptMediaActivity.class)
                    .putExtra(ChatGptMediaActivity.EXTRA_ACTION, action));
            finish();
        });

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(ITEM_SIZE_DP), dp(ITEM_SIZE_DP));
        if (index > 0) {
            lp.topMargin = dp(GAP_DP);
        }
        parent.addView(button, lp);
    }

    private void positionActions() {
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) actions.getLayoutParams();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int edgeMargin = dp(10);
        int gap = dp(8);

        Rect source = getIntent().getSourceBounds();
        int anchorCenterX;
        int anchorTopY;
        if (source != null && !source.isEmpty()) {
            anchorCenterX = source.centerX();
            anchorTopY = source.top;
        } else {
            anchorCenterX = dp(46);
            anchorTopY = height - dp(240);
        }

        int desiredLeft = anchorCenterX - actions.getWidth() / 2;
        int desiredTop = anchorTopY - actions.getHeight() - gap;

        lp.leftMargin = clamp(
                desiredLeft,
                edgeMargin,
                width - actions.getWidth() - edgeMargin);
        lp.topMargin = clamp(
                desiredTop,
                edgeMargin,
                height - actions.getHeight() - edgeMargin);

        actions.setLayoutParams(lp);
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
