package com.aeiou.widgetbar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ProviderTargetTest {
    @Test
    public void fromIdReturnsMatchingProvider() {
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId("google"));
        assertEquals(ProviderTarget.YOUTUBE, ProviderTarget.fromId("YouTube"));
        assertEquals(ProviderTarget.INSTAGRAM, ProviderTarget.fromId("INSTAGRAM"));
        assertEquals(ProviderTarget.TIKTOK, ProviderTarget.fromId("tiktok"));
    }

    @Test
    public void fromIdFallsBackToGoogle() {
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId(null));
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId(""));
        assertEquals(ProviderTarget.GOOGLE, ProviderTarget.fromId("unknown"));
    }
}
