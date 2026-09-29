package com.aeiou.widgetbar;

import java.util.Locale;

enum ProviderTarget {
    GOOGLE(
            "google",
            "Google",
            "com.google.android.googlequicksearchbox",
            "https://www.google.com/search?q=%s"),
    YOUTUBE(
            "youtube",
            "YouTube",
            "com.google.android.youtube",
            "https://www.youtube.com/results?search_query=%s"),
    INSTAGRAM(
            "instagram",
            "Instagram",
            "com.instagram.android",
            "https://www.instagram.com/explore/search/keyword/?q=%s"),
    TIKTOK(
            "tiktok",
            "TikTok",
            "com.zhiliaoapp.musically",
            "https://www.tiktok.com/search?q=%s"),
    CHATGPT(
            "chatgpt",
            "ChatGPT",
            "com.openai.chatgpt",
            null);

    final String id;
    final String label;
    final String packageName;
    final String searchUrl;

    ProviderTarget(
            String id,
            String label,
            String packageName,
            String searchUrl) {
        this.id = id;
        this.label = label;
        this.packageName = packageName;
        this.searchUrl = searchUrl;
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
