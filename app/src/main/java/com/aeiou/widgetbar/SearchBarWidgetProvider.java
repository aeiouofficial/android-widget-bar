package com.aeiou.widgetbar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public final class SearchBarWidgetProvider extends AppWidgetProvider {
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
        ComponentName provider = new ComponentName(context, SearchBarWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(provider)) {
            manager.updateAppWidget(id, buildViews(context, id, editing));
        }
    }

    private static RemoteViews buildViews(Context context, int widgetId, boolean editing) {
        ProviderTarget target = WidgetPrefs.getProvider(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_search_bar);

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
        views.setTextViewText(R.id.search_hint, target.collapsedHint);

        Intent centerIntent = new Intent(context, SearchActivity.class)
                .putExtra(SearchActivity.EXTRA_WIDGET_DOUBLE_TAP, true);
        PendingIntent centerPending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 1,
                centerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent leftIntent = target == ProviderTarget.CHATGPT
                ? new Intent(context, ChatGptActionsActivity.class)
                : new Intent(context, SearchActivity.class);
        PendingIntent leftPending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 2,
                leftIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent selectorIntent = new Intent(context, PickerActivity.class);
        PendingIntent selectorPending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 3,
                selectorIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.provider_icon, leftPending);
        views.setOnClickPendingIntent(R.id.search_hint, centerPending);
        views.setOnClickPendingIntent(R.id.selector_icon, selectorPending);
        return views;
    }
}
