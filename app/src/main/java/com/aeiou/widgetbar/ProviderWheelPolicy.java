package com.aeiou.widgetbar;

final class ProviderWheelPolicy {
    static final int VIRTUAL_ITEM_COUNT = Integer.MAX_VALUE;

    private ProviderWheelPolicy() {}

    static int providerIndexForPosition(int position, int providerCount) {
        if (providerCount <= 0) {
            return 0;
        }
        return Math.floorMod(position, providerCount);
    }

    static int centeredPositionForProvider(int providerOrdinal, int providerCount) {
        if (providerCount <= 0) {
            return 0;
        }

        int center = VIRTUAL_ITEM_COUNT / 2;
        int alignedBase = center - Math.floorMod(center, providerCount);
        int normalizedProvider = Math.floorMod(providerOrdinal, providerCount);
        int result = alignedBase + normalizedProvider;

        // Keep a full provider cycle available on both sides of the anchor.
        if (result < providerCount) {
            result += providerCount;
        }
        if (result > VIRTUAL_ITEM_COUNT - providerCount - 1) {
            result -= providerCount;
        }
        return result;
    }
}
