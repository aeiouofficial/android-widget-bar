package com.aeiou.widgetbar;

final class LauncherPinPolicy {
    static final String AOSP_LAUNCHER_PACKAGE = "com.android.launcher3";

    private LauncherPinPolicy() {
    }

    static boolean requiresManualPlacement(String launcherPackage) {
        return AOSP_LAUNCHER_PACKAGE.equals(launcherPackage);
    }
}
