package com.aeiou.widgetbar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class TapGesturePolicyTest {
    @Test
    public void recognizesSecondTapInsideTimeout() {
        assertTrue(TapGesturePolicy.isDoubleTap(1000L, 1250L, 300L));
        assertTrue(TapGesturePolicy.isDoubleTap(1000L, 1300L, 300L));
    }

    @Test
    public void rejectsLateOrInvalidSecondTap() {
        assertFalse(TapGesturePolicy.isDoubleTap(1000L, 1301L, 300L));
        assertFalse(TapGesturePolicy.isDoubleTap(-1L, 1100L, 300L));
        assertFalse(TapGesturePolicy.isDoubleTap(1200L, 1100L, 300L));
    }
}
