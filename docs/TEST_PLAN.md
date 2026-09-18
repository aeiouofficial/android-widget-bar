# Test plan

## CI gates

1. Gradle 8.13 + AGP 8.13.2 dependency resolution.
2. compileSdk 36 debug build.
3. Android lint with abort-on-error.
4. Debug APK artifact upload.

## Device validation before normal use

1. Install with `adb install -r app-debug.apk`.
2. Launch setup with `adb shell am start -n com.aeiou.widgetbar/.SetupActivity`.
3. Pin the widget using the launcher confirmation.
4. Verify the collapsed widget has one provider icon left, search hint center, and exactly one ChatGPT icon right.
5. Tap left provider icon: only search field + Google/ChatGPT? No: provider picker must contain Google, YouTube, Instagram and TikTok icons; ChatGPT remains a separate right-side action and is not a search provider.
6. Switch each provider and confirm the active icon becomes the left widget icon.
7. Submit one query for each provider and verify the expected installed app or browser fallback opens.
8. Tap the right ChatGPT icon and verify the installed ChatGPT version lands on its root/new-chat composer. If not, capture the installed package's supported links before changing the routing code.
9. Reboot and verify provider selection persists.
10. Uninstall with `adb uninstall com.aeiou.widgetbar`; confirm no launcher/system package was modified.

## Safety rollback

The widget is an ordinary user APK. Removal is a single package uninstall; no system files are changed.
