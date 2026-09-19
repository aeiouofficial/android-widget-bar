package com.aeiou.widgetbar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

public final class WheelSearchBarWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            manager.updateAppWidget(appWidgetId, buildViews(context, appWidgetId, false));
        }
    }

    static void updateAll(Context context) {
        setEditing(context, false);
    }

    static void setEditing(Context context, boolean editing) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, WheelSearchBarWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            manager.updateAppWidget(id, buildViews(context, id, editing));
        }
        if (!editing && ids.length > 0) {
            manager.notifyAppWidgetViewDataChanged(ids, R.id.provider_wheel);
        }
    }

    private static RemoteViews buildViews(Context context, int widgetId, boolean editing) {
        ProviderTarget target = WidgetPrefs.getProvider(context);
        ProviderMode mode = WidgetPrefs.getMode(context, target);
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                R.layout.widget_search_bar_wheel);

        views.setViewVisibility(android.R.id.background, editing ? View.INVISIBLE : View.VISIBLE);
        if (editing) {
            return views;
        }

        int iconPx = Math.max(
                96,
                Math.round(context.getResources().getDisplayMetrics().density * 48f));
        views.setImageViewBitmap(
                R.id.provider_icon,
                IconLoader.load(
                        context,
                        target.packageName,
                        iconPx,
                        target.label.substring(0, 1)));
        views.setTextViewText(R.id.search_hint, mode.collapsedHint);

        Intent serviceIntent = new Intent(context, ProviderWheelService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.provider_wheel, serviceIntent);
        views.setScrollPosition(
                R.id.provider_wheel,
                ProviderWheelPolicy.centeredPositionForProvider(
                        target.ordinal(),
                        ProviderTarget.values().length));

        Intent centerIntent = new Intent(context, SearchActivity.class)
                .putExtra(SearchActivity.EXTRA_WIDGET_DOUBLE_TAP, true)
                .putExtra(SearchActivity.EXTRA_WIDGET_VARIANT, WidgetUpdates.VARIANT_WHEEL);
        PendingIntent centerPending = PendingIntent.getActivity(
                context,
                widgetId * 100 + 1,
                centerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent leftModeIntent = new Intent(context, ModePickerActivity.class)
                .putExtra(ModePickerActivity.EXTRA_ANCHOR_SIDE, ModePickerActivity.ANCHOR_LEFT);
        PendingIntent leftModePending = PendingIntent.getActivity(
                context,
                widgetId * 100 + 2,
                leftModeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent wheelTemplate = new Intent(context, ModePickerActivity.class)
                .putExtra(ModePickerActivity.EXTRA_ANCHOR_SIDE, ModePickerActivity.ANCHOR_RIGHT)
                .putExtra(ModePickerActivity.EXTRA_FROM_WHEEL, true);
        int templateFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            templateFlags |= PendingIntent.FLAG_MUTABLE;
        }
        PendingIntent wheelPending = PendingIntent.getActivity(
                context,
                widgetId * 100 + 3,
                wheelTemplate,
                templateFlags);

        views.setOnClickPendingIntent(R.id.provider_icon, leftModePending);
        views.setOnClickPendingIntent(R.id.search_hint, centerPending);
        views.setPendingIntentTemplate(R.id.provider_wheel, wheelPending);
        return views;
    }
}
