package com.aeiou.widgetbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

enum ProviderMode {
    GOOGLE_SEARCH(
            "google_search",
            ProviderTarget.GOOGLE,
            "Search",
            "Search the web…",
            "Search the web…",
            true,
            R.drawable.ic_mode_search),
    GOOGLE_GEMINI(
            "google_gemini",
            ProviderTarget.GOOGLE,
            "Gemini",
            "Ask Gemini…",
            "Ask Gemini…",
            true,
            R.drawable.ic_mode_gemini),

    YOUTUBE_SEARCH(
            "youtube_search",
            ProviderTarget.YOUTUBE,
            "Search",
            "Search YouTube…",
            "Search YouTube…",
            true,
            R.drawable.ic_mode_search),
    YOUTUBE_SHORTS(
            "youtube_shorts",
            ProviderTarget.YOUTUBE,
            "Shorts",
            "Open Shorts",
            "Open Shorts",
            false,
            R.drawable.ic_mode_shorts),
    YOUTUBE_SUBSCRIPTIONS(
            "youtube_subscriptions",
            ProviderTarget.YOUTUBE,
            "Subscriptions",
            "Open subscriptions",
            "Open subscriptions",
            false,
            R.drawable.ic_mode_subscriptions),

    INSTAGRAM_SEARCH(
            "instagram_search",
            ProviderTarget.INSTAGRAM,
            "Search",
            "Search Instagram…",
            "Search Instagram…",
            true,
            R.drawable.ic_mode_search),
    INSTAGRAM_STORY(
            "instagram_story",
            ProviderTarget.INSTAGRAM,
            "Story",
            "Create story",
            "Create story",
            false,
            R.drawable.ic_mode_story),
    INSTAGRAM_REEL(
            "instagram_reel",
            ProviderTarget.INSTAGRAM,
            "Reel",
            "Create reel",
            "Create reel",
            false,
            R.drawable.ic_mode_reel),
    INSTAGRAM_MESSAGES(
            "instagram_messages",
            ProviderTarget.INSTAGRAM,
            "Messages",
            "Open messages",
            "Open messages",
            false,
            R.drawable.ic_mode_messages),

    TIKTOK_SEARCH(
            "tiktok_search",
            ProviderTarget.TIKTOK,
            "Search",
            "Search TikTok…",
            "Search TikTok…",
            true,
            R.drawable.ic_mode_search),
    TIKTOK_CREATE(
            "tiktok_create",
            ProviderTarget.TIKTOK,
            "Create",
            "Create TikTok",
            "Create TikTok",
            false,
            R.drawable.ic_mode_create),
    TIKTOK_INBOX(
            "tiktok_inbox",
            ProviderTarget.TIKTOK,
            "Inbox",
            "Open inbox",
            "Open inbox",
            false,
            R.drawable.ic_mode_messages),

    CHATGPT_NEW_CHAT(
            "chatgpt_new_chat",
            ProviderTarget.CHATGPT,
            "New chat",
            "New chat",
            "Ask ChatGPT…",
            true,
            R.drawable.ic_mode_chat),
    CHATGPT_VOICE(
            "chatgpt_voice",
            ProviderTarget.CHATGPT,
            "Voice",
            "Start voice",
            "Start voice",
            false,
            R.drawable.ic_voice),
    CHATGPT_CAMERA(
            "chatgpt_camera",
            ProviderTarget.CHATGPT,
            "Camera",
            "Take photo",
            "Take photo",
            false,
            R.drawable.ic_camera),
    CHATGPT_PHOTO(
            "chatgpt_photo",
            ProviderTarget.CHATGPT,
            "Photo",
            "Choose photo",
            "Choose photo",
            false,
            R.drawable.ic_image),
    CHATGPT_DICTATION(
            "chatgpt_dictation",
            ProviderTarget.CHATGPT,
            "Dictation",
            "Start dictation",
            "Start dictation",
            false,
            R.drawable.ic_mic);

    final String id;
    final ProviderTarget provider;
    final String label;
    final String collapsedHint;
    final String inputHint;
    final boolean acceptsText;
    final int iconRes;

    ProviderMode(
            String id,
            ProviderTarget provider,
            String label,
            String collapsedHint,
            String inputHint,
            boolean acceptsText,
            int iconRes) {
        this.id = id;
        this.provider = provider;
        this.label = label;
        this.collapsedHint = collapsedHint;
        this.inputHint = inputHint;
        this.acceptsText = acceptsText;
        this.iconRes = iconRes;
    }

    static ProviderMode defaultFor(ProviderTarget provider) {
        switch (provider) {
            case YOUTUBE:
                return YOUTUBE_SEARCH;
            case INSTAGRAM:
                return INSTAGRAM_SEARCH;
            case TIKTOK:
                return TIKTOK_SEARCH;
            case CHATGPT:
                return CHATGPT_NEW_CHAT;
            case GOOGLE:
            default:
                return GOOGLE_SEARCH;
        }
    }

    static ProviderMode fromId(ProviderTarget provider, String value) {
        if (value == null) return defaultFor(provider);
        String normalized = value.toLowerCase(Locale.ROOT);
        for (ProviderMode mode : values()) {
            if (mode.provider == provider && mode.id.equals(normalized)) {
                return mode;
            }
        }
        return defaultFor(provider);
    }

    static ProviderMode[] modesFor(ProviderTarget provider) {
        List<ProviderMode> result = new ArrayList<>();
        for (ProviderMode mode : values()) {
            if (mode.provider == provider) {
                result.add(mode);
            }
        }
        return result.toArray(new ProviderMode[0]);
    }
}
