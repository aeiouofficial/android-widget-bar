package com.aeiou.widgetbar;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

final class SearchLauncher {
    private static final String GOOGLE_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String YOUTUBE_PACKAGE = "com.google.android.youtube";
    private static final String INSTAGRAM_PACKAGE = "com.instagram.android";
    private static final String TIKTOK_PACKAGE = "com.zhiliaoapp.musically";

    private SearchLauncher() {}

    static boolean hasSubmitText(String query) {
        return query != null && !query.trim().isEmpty();
    }

    static boolean launch(Context context, ProviderMode mode, String query) {
        if (!mode.acceptsText || !hasSubmitText(query)) {
            return false;
        }

        String trimmed = query.trim();

        switch (mode) {
            case GOOGLE_GEMINI:
                return launchGeminiQuestion(context, trimmed);
            case CHATGPT_NEW_CHAT:
                Intent create = new Intent(context, ChatGptNewChatActivity.class)
                        .putExtra(ChatGptNewChatActivity.EXTRA_PROMPT, trimmed)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(create);
                return true;
            case GOOGLE_SEARCH:
                return launchGoogleSearch(context, trimmed);
            case YOUTUBE_SEARCH:
            case INSTAGRAM_SEARCH:
            case TIKTOK_SEARCH:
                return launchSearchUrl(context, mode.provider, trimmed);
            default:
                return false;
        }
    }

    static boolean launchAction(Context context, ProviderMode mode) {
        Intent intent;

        switch (mode) {
            case YOUTUBE_SHORTS:
                intent = new Intent("com.google.android.youtube.action.open.shorts")
                        .setPackage(YOUTUBE_PACKAGE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return startOrUrlFallback(
                        context,
                        intent,
                        "https://www.youtube.com/shorts");

            case YOUTUBE_SUBSCRIPTIONS:
                intent = new Intent("com.google.android.youtube.action.open.subscriptions")
                        .setPackage(YOUTUBE_PACKAGE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return startOrUrlFallback(
                        context,
                        intent,
                        "https://www.youtube.com/feed/subscriptions");

            case INSTAGRAM_STORY:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://story-camera"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/");

            case INSTAGRAM_REEL:
                intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("instagram://reels-camera"))
                        .setPackage(INSTAGRAM_PACKAGE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                    return true;
                }
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://reels"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/reels/");

            case INSTAGRAM_MESSAGES:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://direct-inbox"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/direct/inbox/");

            case TIKTOK_CREATE:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("snssdk1233://aweme/create"),
                        TIKTOK_PACKAGE,
                        "https://www.tiktok.com/");

            case TIKTOK_INBOX:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("snssdk1233://aweme/notification"),
                        TIKTOK_PACKAGE,
                        "https://www.tiktok.com/");

            case CHATGPT_VOICE:
                return launchChatGptMedia(context, ChatGptMediaActivity.ACTION_VOICE);
            case CHATGPT_CAMERA:
                return launchChatGptMedia(context, ChatGptMediaActivity.ACTION_CAMERA);
            case CHATGPT_PHOTO:
                return launchChatGptMedia(context, ChatGptMediaActivity.ACTION_PHOTO);
            case CHATGPT_DICTATION:
                return launchChatGptMedia(context, ChatGptMediaActivity.ACTION_DICTATION);

            default:
                return false;
        }
    }

    static boolean openAppHome(Context context, ProviderTarget target) {
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(target.packageName);
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
            return true;
        }

        String url;
        switch (target) {
            case YOUTUBE:
                url = "https://www.youtube.com/";
                break;
            case INSTAGRAM:
                url = "https://www.instagram.com/";
                break;
            case TIKTOK:
                url = "https://www.tiktok.com/";
                break;
            case CHATGPT:
                url = "https://chatgpt.com/";
                break;
            case GOOGLE:
            default:
                url = "https://www.google.com/";
                break;
        }

        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }

    private static boolean launchGoogleSearch(Context context, String query) {
        Intent webSearch = new Intent(Intent.ACTION_WEB_SEARCH);
        webSearch.putExtra(SearchManager.QUERY, query);
        webSearch.setPackage(GOOGLE_PACKAGE);
        webSearch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (webSearch.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(webSearch);
            return true;
        }

        return startViewWithPackageFallback(
                context,
                Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)),
                GOOGLE_PACKAGE,
                "https://www.google.com/search?q=" + Uri.encode(query));
    }

    private static boolean launchGeminiQuestion(Context context, String query) {
        Intent processText = new Intent(Intent.ACTION_PROCESS_TEXT)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_PROCESS_TEXT, query)
                .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                .setPackage(GOOGLE_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (processText.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(processText);
            return true;
        }

        return startViewWithPackageFallback(
                context,
                Uri.parse("https://gemini.google.com/app"),
                GOOGLE_PACKAGE,
                "https://gemini.google.com/app");
    }

    private static boolean launchSearchUrl(
            Context context,
            ProviderTarget target,
            String query) {
        String url = String.format(target.searchUrl, Uri.encode(query));
        return startViewWithPackageFallback(
                context,
                Uri.parse(url),
                target.packageName,
                url);
    }

    private static boolean launchChatGptMedia(Context context, String action) {
        Intent intent = new Intent(context, ChatGptMediaActivity.class)
                .putExtra(ChatGptMediaActivity.EXTRA_ACTION, action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

    private static boolean startOrUrlFallback(
            Context context,
            Intent intent,
            String fallbackUrl) {
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        }

        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }

    private static boolean startViewWithPackageFallback(
            Context context,
            Uri appUri,
            String packageName,
            String fallbackUrl) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, appUri)
                .setPackage(packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (appIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(appIntent);
            return true;
        }

        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return true;
    }
}
