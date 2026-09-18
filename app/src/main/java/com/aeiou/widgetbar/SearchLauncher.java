package com.aeiou.widgetbar;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

final class SearchLauncher {
    private SearchLauncher() {}

    static boolean hasSubmitText(String query) {
        return query != null && !query.trim().isEmpty();
    }

    static boolean launch(Context context, ProviderTarget target, String query) {
        if (!hasSubmitText(query)) {
            return false;
        }

        String trimmed = query.trim();

        if (target.createAction) {
            Intent create = new Intent(context, ChatGptNewChatActivity.class)
                    .putExtra(ChatGptNewChatActivity.EXTRA_PROMPT, trimmed)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(create);
            return true;
        }

        if (target == ProviderTarget.GOOGLE) {
            Intent webSearch = new Intent(Intent.ACTION_WEB_SEARCH);
            webSearch.putExtra(SearchManager.QUERY, trimmed);
            webSearch.setPackage(target.packageName);
            webSearch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (webSearch.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webSearch);
                return true;
            }
        }

        String url = String.format(target.searchUrl, Uri.encode(trimmed));
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        appIntent.setPackage(target.packageName);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (appIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(appIntent);
            return true;
        }

        Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(fallback);
        return true;
    }
}
