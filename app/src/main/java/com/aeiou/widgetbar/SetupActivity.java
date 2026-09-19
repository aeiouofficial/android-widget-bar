package com.aeiou.widgetbar;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class SetupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        boolean manualPlacement = requiresManualPlacement();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(0xFF10191D);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView body = new TextView(this);
        body.setText(manualPlacement ? R.string.setup_body_manual : R.string.setup_body);
        body.setTextColor(0xFFC9D6D8);
        body.setTextSize(16f);
        body.setGravity(Gravity.CENTER);
        root.addView(body);

        if (manualPlacement) {
            Button openHome = buildButton(R.string.open_home_screen);
            openHome.setOnClickListener(v -> openHomeForManualPlacement());
            root.addView(openHome, buttonParams(true));
            return root;
        }

        Button classic = buildButton(R.string.add_classic_widget);
        classic.setOnClickListener(v -> requestWidgetPin(SearchBarWidgetProvider.class));
        root.addView(classic, buttonParams(true));

        Button wheel = buildButton(R.string.add_wheel_widget);
        wheel.setOnClickListener(v -> requestWidgetPin(WheelSearchBarWidgetProvider.class));
        root.addView(wheel, buttonParams(false));
        return root;
    }

    private Button buildButton(int labelRes) {
        Button button = new Button(this);
        button.setText(labelRes);
        return button;
    }

    private LinearLayout.LayoutParams buttonParams(boolean first) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54));
        lp.setMargins(0, dp(first ? 28 : 12), 0, 0);
        return lp;
    }

    private boolean requiresManualPlacement() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        ResolveInfo home = getPackageManager()
                .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        String launcherPackage = home != null && home.activityInfo != null
                ? home.activityInfo.packageName
                : null;
        return LauncherPinPolicy.requiresManualPlacement(launcherPackage);
    }

    private void requestWidgetPin(Class<? extends AppWidgetProvider> providerClass) {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, providerClass);
        if (!manager.isRequestPinAppWidgetSupported()
                || !manager.requestPinAppWidget(provider, null, null)) {
            openHomeForManualPlacement();
        }
    }

    private void openHomeForManualPlacement() {
        Toast.makeText(this, R.string.manual_placement_steps, Toast.LENGTH_LONG).show();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
