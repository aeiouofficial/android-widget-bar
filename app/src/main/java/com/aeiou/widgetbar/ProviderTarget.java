package com.aeiou.widgetbar;

import java.util.Locale;

enum ProviderTarget {
    GOOGLE(
            "google",
            "Google",
            "Search the web…",
            "Search the web…",
            "com.google.android.googlequicksearchbox",
            "https://www.google.com/search?q=%s",
            false),
    YOUTUBE(
            "youtube",
            "YouTube",
            "Search YouTube…",
            "Search YouTube…",
            "com.google.android.youtube",
            "https://www.youtube.com/results?search_query=%s",
            false),
    INSTAGRAM(
            "instagram",
            "Instagram",
            "Search Instagram…",
            "Search Instagram…",
            "com.instagram.android",
            "https://www.instagram.com/explore/search/keyword/?q=%s",
            false),
    TIKTOK(
            "tiktok",
            "TikTok",
            "Search TikTok…",
            "Search TikTok…",
            "com.zhiliaoapp.musically",
            "https://www.tiktok.com/search?q=%s",
            false),
    CHATGPT(
            "chatgpt",
            "ChatGPT",
            "New chat",
            "Ask ChatGPT…",
            "com.openai.chatgpt",
            null,
            true);

    final String id;
    final String label;
    final String collapsedHint;
    final String inputHint;
    final String packageName;
    final String searchUrl;
    final boolean createAction;

    ProviderTarget(
            String id,
            String label,
            String collapsedHint,
            String inputHint,
            String packageName,
            String searchUrl,
            boolean createAction) {
        this.id = id;
        this.label = label;
        this.collapsedHint = collapsedHint;
        this.inputHint = inputHint;
        this.packageName = packageName;
        this.searchUrl = searchUrl;
        this.createAction = createAction;
    }

    static ProviderTarget fromId(String value) {
        if (value == null) return GOOGLE;
        String normalized = value.toLowerCase(Locale.ROOT);
        for (ProviderTarget target : values()) {
            if (target.id.equals(normalized)) return target;
        }
        return GOOGLE;
    }
}
