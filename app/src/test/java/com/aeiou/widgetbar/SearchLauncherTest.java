package com.aeiou.widgetbar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SearchLauncherTest {
    @Test
    public void providerLaunchRequiresTypedSubmitText() {
        assertFalse(SearchLauncher.hasSubmitText(null));
        assertFalse(SearchLauncher.hasSubmitText(""));
        assertFalse(SearchLauncher.hasSubmitText("   "));
        assertTrue(SearchLauncher.hasSubmitText("hello"));
        assertTrue(SearchLauncher.hasSubmitText("  hello  "));
    }
}
