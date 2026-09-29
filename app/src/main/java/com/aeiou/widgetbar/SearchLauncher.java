package com.aeiou.widgetbar;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

final class SearchLauncher {
    private static final String TAG = "WidgetBarLaunch";
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
                return startSafely(
                        context,
                        new Intent(context, ChatGptNewChatActivity.class)
                                .putExtra(ChatGptNewChatActivity.EXTRA_PROMPT, trimmed)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        "chatgpt-new-chat");
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
                        "https://www.youtube.com/shorts",
                        "youtube-shorts");

            case YOUTUBE_SUBSCRIPTIONS:
                intent = new Intent("com.google.android.youtube.action.open.subscriptions")
                        .setPackage(YOUTUBE_PACKAGE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return startOrUrlFallback(
                        context,
                        intent,
                        "https://www.youtube.com/feed/subscriptions",
                        "youtube-subscriptions");

            case INSTAGRAM_STORY:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://story-camera"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/",
                        "instagram-story");

            case INSTAGRAM_REEL:
                intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("instagram://reels-camera"))
                        .setPackage(INSTAGRAM_PACKAGE)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (canResolve(context, intent)
                        && startSafely(context, intent, "instagram-reel-camera")) {
                    return true;
                }
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://reels"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/reels/",
                        "instagram-reels");

            case INSTAGRAM_MESSAGES:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("instagram://direct-inbox"),
                        INSTAGRAM_PACKAGE,
                        "https://www.instagram.com/direct/inbox/",
                        "instagram-messages");

            case TIKTOK_CREATE:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("snssdk1233://aweme/create"),
                        TIKTOK_PACKAGE,
                        "https://www.tiktok.com/",
                        "tiktok-create");

            case TIKTOK_INBOX:
                return startViewWithPackageFallback(
                        context,
                        Uri.parse("snssdk1233://aweme/notification"),
                        TIKTOK_PACKAGE,
                        "https://www.tiktok.com/",
                        "tiktok-inbox");

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
            if (startSafely(context, launch, target.id + "-home")) {
                return true;
            }
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

        return openWeb(context, url, target.id + "-home-web");
    }

    private static boolean launchGoogleSearch(Context context, String query) {
        Intent webSearch = new Intent(Intent.ACTION_WEB_SEARCH);
        webSearch.putExtra(SearchManager.QUERY, query);
        webSearch.setPackage(GOOGLE_PACKAGE);
        webSearch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (canResolve(context, webSearch)
                && startSafely(context, webSearch, "google-search")) {
            return true;
        }

        String url = "https://www.google.com/search?q=" + Uri.encode(query);
        return startViewWithPackageFallback(
                context,
                Uri.parse(url),
                GOOGLE_PACKAGE,
                url,
                "google-search-web");
    }

    private static boolean launchGeminiQuestion(Context context, String query) {
        Intent processText = new Intent(Intent.ACTION_PROCESS_TEXT)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_PROCESS_TEXT, query)
                .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                .setPackage(GOOGLE_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (canResolve(context, processText)
                && startSafely(context, processText, "google-gemini-process-text")) {
            return true;
        }

        return startViewWithPackageFallback(
                context,
                Uri.parse("https://gemini.google.com/app"),
                GOOGLE_PACKAGE,
                "https://gemini.google.com/app",
                "google-gemini-web");
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
                url,
                target.id + "-search");
    }

    private static boolean launchChatGptMedia(Context context, String action) {
        Intent intent = new Intent(context, ChatGptMediaActivity.class)
                .putExtra(ChatGptMediaActivity.EXTRA_ACTION, action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return startSafely(context, intent, "chatgpt-media-" + action);
    }

    private static boolean startOrUrlFallback(
            Context context,
            Intent intent,
            String fallbackUrl,
            String route) {
        if (canResolve(context, intent)
                && startSafely(context, intent, route)) {
            return true;
        }
        return openWeb(context, fallbackUrl, route + "-web");
    }

    private static boolean startViewWithPackageFallback(
            Context context,
            Uri appUri,
            String packageName,
            String fallbackUrl,
            String route) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, appUri)
                .setPackage(packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (canResolve(context, appIntent)
                && startSafely(context, appIntent, route)) {
            return true;
        }

        return openWeb(context, fallbackUrl, route + "-web");
    }

    private static boolean openWeb(Context context, String url, String route) {
        Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return startSafely(context, web, route);
    }

    private static boolean canResolve(Context context, Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    static boolean startSafely(Context context, Intent intent, String route) {
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(
                    TAG,
                    "launch_failed route=" + route
                            + " package=" + intent.getPackage()
                            + " component=" + intent.getComponent()
                            + " cause=" + e.getClass().getSimpleName());
            return false;
        }
    }
}
