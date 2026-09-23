package com.aeiou.widgetbar;

import android.content.Context;

final class WidgetUpdates {
    static final String VARIANT_CLASSIC = "classic";
    static final String VARIANT_WHEEL = "wheel";

    private WidgetUpdates() {}

    static void updateAll(Context context) {
        SearchBarWidgetProvider.updateAll(context);
        WheelSearchBarWidgetProvider.updateAll(context);
    }

    static void setEditing(Context context, String variant, boolean editing) {
        if (VARIANT_WHEEL.equals(variant)) {
            WheelSearchBarWidgetProvider.setEditing(context, editing);
        } else {
            SearchBarWidgetProvider.setEditing(context, editing);
        }
    }
}
