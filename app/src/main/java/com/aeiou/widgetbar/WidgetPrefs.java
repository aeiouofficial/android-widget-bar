package com.aeiou.widgetbar;

import android.content.Context;
import android.content.SharedPreferences;

final class WidgetPrefs {
    private static final String PREFS = "widget_bar";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_MODE_PREFIX = "mode_";

    private WidgetPrefs() {}

    static ProviderTarget getProvider(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return ProviderTarget.fromId(prefs.getString(KEY_PROVIDER, ProviderTarget.GOOGLE.id));
    }

    static void setProvider(Context context, ProviderTarget target) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROVIDER, target.id)
                .apply();
    }

    static ProviderMode getMode(Context context, ProviderTarget target) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String value = prefs.getString(
                KEY_MODE_PREFIX + target.id,
                ProviderMode.defaultFor(target).id);
        return ProviderMode.fromId(target, value);
    }

    static void setMode(Context context, ProviderMode mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MODE_PREFIX + mode.provider.id, mode.id)
                .apply();
    }
}
