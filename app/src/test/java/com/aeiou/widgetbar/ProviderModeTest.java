package com.aeiou.widgetbar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ProviderModeTest {
    @Test
    public void eachProviderDefaultsToItsPrimaryTextMode() {
        assertEquals(ProviderMode.GOOGLE_SEARCH, ProviderMode.defaultFor(ProviderTarget.GOOGLE));
        assertEquals(ProviderMode.YOUTUBE_SEARCH, ProviderMode.defaultFor(ProviderTarget.YOUTUBE));
        assertEquals(ProviderMode.INSTAGRAM_SEARCH, ProviderMode.defaultFor(ProviderTarget.INSTAGRAM));
        assertEquals(ProviderMode.TIKTOK_SEARCH, ProviderMode.defaultFor(ProviderTarget.TIKTOK));
        assertEquals(ProviderMode.CHATGPT_NEW_CHAT, ProviderMode.defaultFor(ProviderTarget.CHATGPT));
    }

    @Test
    public void providerScopedLookupRejectsModeFromAnotherProvider() {
        assertEquals(
                ProviderMode.GOOGLE_SEARCH,
                ProviderMode.fromId(ProviderTarget.GOOGLE, "youtube_shorts"));
        assertEquals(
                ProviderMode.INSTAGRAM_REEL,
                ProviderMode.fromId(ProviderTarget.INSTAGRAM, "INSTAGRAM_REEL"));
    }

    @Test
    public void requestedModeSetsArePresent() {
        assertEquals(2, ProviderMode.modesFor(ProviderTarget.GOOGLE).length);
        assertEquals(3, ProviderMode.modesFor(ProviderTarget.YOUTUBE).length);
        assertEquals(4, ProviderMode.modesFor(ProviderTarget.INSTAGRAM).length);
        assertEquals(3, ProviderMode.modesFor(ProviderTarget.TIKTOK).length);
        assertEquals(5, ProviderMode.modesFor(ProviderTarget.CHATGPT).length);
    }

    @Test
    public void textAndImmediateModesAreSeparated() {
        assertTrue(ProviderMode.GOOGLE_GEMINI.acceptsText);
        assertTrue(ProviderMode.CHATGPT_NEW_CHAT.acceptsText);
        assertFalse(ProviderMode.YOUTUBE_SHORTS.acceptsText);
        assertFalse(ProviderMode.INSTAGRAM_STORY.acceptsText);
        assertFalse(ProviderMode.INSTAGRAM_REEL.acceptsText);
        assertFalse(ProviderMode.INSTAGRAM_MESSAGES.acceptsText);
        assertFalse(ProviderMode.TIKTOK_CREATE.acceptsText);
        assertFalse(ProviderMode.CHATGPT_CAMERA.acceptsText);
    }
}
