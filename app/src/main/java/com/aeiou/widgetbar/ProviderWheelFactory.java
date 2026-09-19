package com.aeiou.widgetbar;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public final class ProviderWheelFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final ProviderTarget[] providers = ProviderTarget.values();

    ProviderWheelFactory(Context context) {
        this.context = context;
    }

    @Override public void onCreate() {}
    @Override public void onDataSetChanged() {}
    @Override public void onDestroy() {}

    @Override
    public int getCount() {
        return ProviderWheelPolicy.VIRTUAL_ITEM_COUNT;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (providers.length == 0) {
            return null;
        }

        ProviderTarget target = providers[
                ProviderWheelPolicy.providerIndexForPosition(position, providers.length)];

        RemoteViews item = new RemoteViews(
                context.getPackageName(),
                R.layout.widget_provider_wheel_item);

        int iconPx = Math.max(
                96,
                Math.round(context.getResources().getDisplayMetrics().density * 48f));
        item.setImageViewBitmap(
                R.id.wheel_icon,
                IconLoader.load(
                        context,
                        target.packageName,
                        iconPx,
                        target.label.substring(0, 1)));
        item.setContentDescription(R.id.wheel_icon, target.label);

        Intent fillIn = new Intent()
                .putExtra(ModePickerActivity.EXTRA_PROVIDER_ID, target.id)
                .putExtra(ModePickerActivity.EXTRA_ANCHOR_SIDE, ModePickerActivity.ANCHOR_RIGHT)
                .putExtra(ModePickerActivity.EXTRA_FROM_WHEEL, true);
        item.setOnClickFillInIntent(R.id.wheel_item_root, fillIn);
        item.setOnClickFillInIntent(R.id.wheel_icon, fillIn);
        return item;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return ProviderWheelPolicy.providerIndexForPosition(position, providers.length);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
