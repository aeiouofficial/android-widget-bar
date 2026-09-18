package com.aeiou.widgetbar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LauncherPinPolicyTest {
    @Test
    public void aospLauncherUsesManualPlacement() {
        assertTrue(LauncherPinPolicy.requiresManualPlacement("com.android.launcher3"));
    }

    @Test
    public void otherLaunchersKeepSystemPinFlow() {
        assertFalse(LauncherPinPolicy.requiresManualPlacement("com.google.android.apps.nexuslauncher"));
        assertFalse(LauncherPinPolicy.requiresManualPlacement("com.miui.home"));
        assertFalse(LauncherPinPolicy.requiresManualPlacement(null));
    }
}
