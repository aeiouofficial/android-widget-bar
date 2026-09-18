package com.aeiou.widgetbar;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

final class SearchLauncher {
    private SearchLauncher() {}

    static void launch(Context context, ProviderTarget target, String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            openAppHome(context, target);
            return;
        }

        if (target == ProviderTarget.GOOGLE) {
            Intent webSearch = new Intent(Intent.ACTION_WEB_SEARCH);
            webSearch.putExtra(SearchManager.QUERY, trimmed);
            webSearch.setPackage(target.packageName);
            webSearch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (webSearch.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webSearch);
                return;
            }
        }

        String url = String.format(target.searchUrl, Uri.encode(trimmed));
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        appIntent.setPackage(target.packageName);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (appIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(appIntent);
            return;
        }

        Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(fallback);
    }

    private static void openAppHome(Context context, ProviderTarget target) {
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(target.packageName);
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
            return;
        }

        String home;
        switch (target) {
            case YOUTUBE:
                home = "https://www.youtube.com/";
                break;
            case INSTAGRAM:
                home = "https://www.instagram.com/";
                break;
            case TIKTOK:
                home = "https://www.tiktok.com/";
                break;
            case GOOGLE:
            default:
                home = "https://www.google.com/";
                break;
        }
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(home));
        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browser);
    }
}
