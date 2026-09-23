package com.aeiou.widgetbar;

import android.content.Intent;
import android.widget.RemoteViewsService;

public final class ProviderWheelService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ProviderWheelFactory(getApplicationContext());
    }
}
