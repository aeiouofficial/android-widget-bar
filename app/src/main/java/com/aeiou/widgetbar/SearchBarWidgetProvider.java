package com.aeiou.widgetbar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public final class SearchBarWidgetProvider extends AppWidgetProvider {
    static final String EXTRA_FOCUS_SEARCH = "focus_search";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            manager.updateAppWidget(appWidgetId, buildViews(context, appWidgetId));
        }
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, SearchBarWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            manager.updateAppWidget(id, buildViews(context, id));
        }
    }

    private static RemoteViews buildViews(Context context, int widgetId) {
        ProviderTarget target = WidgetPrefs.getProvider(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_search_bar);

        int iconPx = Math.max(96, Math.round(context.getResources().getDisplayMetrics().density * 48f));
        views.setImageViewBitmap(R.id.provider_icon,
                IconLoader.load(context, target.packageName, iconPx, target.label.substring(0, 1)));
        views.setImageViewBitmap(R.id.chatgpt_icon,
                IconLoader.load(context, "com.openai.chatgpt", iconPx, "AI"));
        views.setTextViewText(R.id.search_hint, target.hint);

        Intent chooseIntent = new Intent(context, PickerActivity.class)
                .putExtra(EXTRA_FOCUS_SEARCH, false);
        PendingIntent choosePending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 1,
                chooseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent searchIntent = new Intent(context, PickerActivity.class)
                .putExtra(EXTRA_FOCUS_SEARCH, true);
        PendingIntent searchPending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 2,
                searchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent chatIntent = new Intent(context, ChatGptNewChatActivity.class);
        PendingIntent chatPending = PendingIntent.getActivity(
                context,
                widgetId * 10 + 3,
                chatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.provider_icon, choosePending);
        views.setOnClickPendingIntent(R.id.search_hint, searchPending);
        views.setOnClickPendingIntent(R.id.chatgpt_icon, chatPending);
        return views;
    }
}
