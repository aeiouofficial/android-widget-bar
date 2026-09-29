package com.aeiou.widgetbar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class ProviderTargetTest {
    @Test
    public void fromIdReturnsMatchingProvider() {
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId("google"));
        assertEquals(ProviderTarget.YOUTUBE, ProviderTarget.fromId("YouTube"));
        assertEquals(ProviderTarget.INSTAGRAM, ProviderTarget.fromId("INSTAGRAM"));
        assertEquals(ProviderTarget.TIKTOK, ProviderTarget.fromId("tiktok"));
        assertEquals(ProviderTarget.CHATGPT, ProviderTarget.fromId("ChatGPT"));
    }

    @Test
    public void fromIdFallsBackToGoogle() {
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId(null));
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId(""));
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId("unknown"));
    }

    @Test
    public void chatGptHasNoSearchUrl() {
        assertNull(ProviderTarget.CHATGPT.searchUrl);
        assertEquals("com.openai.chatgpt", ProviderTarget.CHATGPT.packageName);
    }
}
