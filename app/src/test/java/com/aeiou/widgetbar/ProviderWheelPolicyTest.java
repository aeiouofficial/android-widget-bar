package com.aeiou.widgetbar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ProviderWheelPolicyTest {
    @Test
    public void virtualRailRepeatsProvidersForeverInBothDirections() {
        int providers = 5;
        int anchor = ProviderWheelPolicy.centeredPositionForProvider(0, providers);

        for (int delta = -25; delta <= 25; delta++) {
            int expected = Math.floorMod(delta, providers);
            assertEquals(
                    expected,
                    ProviderWheelPolicy.providerIndexForPosition(anchor + delta, providers));
        }
    }

    @Test
    public void anchorStartsFarFromEitherPhysicalEnd() {
        int providers = 5;
        int anchor = ProviderWheelPolicy.centeredPositionForProvider(3, providers);

        assertTrue(anchor > 1_000_000_000);
        assertTrue(ProviderWheelPolicy.VIRTUAL_ITEM_COUNT - anchor > 1_000_000_000);
        assertEquals(
                3,
                ProviderWheelPolicy.providerIndexForPosition(anchor, providers));
    }
}
