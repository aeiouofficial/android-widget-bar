package com.aeiou.widgetbar;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
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
        body.setText(R.string.setup_body);
        body.setTextColor(0xFFC9D6D8);
        body.setTextSize(16f);
        body.setGravity(Gravity.CENTER);
        root.addView(body);

        Button add = new Button(this);
        add.setText(R.string.add_widget);
        add.setOnClickListener(v -> requestWidgetPin());
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        buttonLp.setMargins(0, dp(28), 0, 0);
        root.addView(add, buttonLp);
        return root;
    }

    private void requestWidgetPin() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, SearchBarWidgetProvider.class);
        if (!manager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, R.string.pin_unsupported, Toast.LENGTH_LONG).show();
            return;
        }

        Intent callbackIntent = new Intent(this, SetupActivity.class);
        PendingIntent success = PendingIntent.getActivity(
                this, 100, callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.requestPinAppWidget(provider, null, success);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
