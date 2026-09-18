# Test plan

## CI gates

1. Source contract check: XML parse, zero requested permissions, one collapsed right-side ChatGPT icon, four search providers.
2. Gradle 8.13 + AGP 8.13.2 dependency resolution.
3. compileSdk 36 debug build.
4. Android lint with abort-on-error.
5. Debug APK artifact upload.

## Device validation before normal use

1. Install with `adb install -r app-debug.apk`.
2. Launch setup with `adb shell am start -n com.aeiou.widgetbar/.SetupActivity`.
3. Pin the widget using the launcher confirmation.
4. Verify the collapsed widget has one active provider icon left, search hint center, and exactly one ChatGPT icon right.
5. Tap the left provider icon: the drop-up must contain only the search field plus Google, YouTube, Instagram and TikTok icons.
6. Switch each provider and confirm the selected icon immediately becomes the persistent left widget icon.
7. Submit one query for each provider and verify the expected installed app or browser fallback opens.
8. Tap the right ChatGPT icon and verify the installed ChatGPT version lands on a fresh/new-chat composer. If it does not, inspect that installed app version's supported App Links before changing the route.
9. Reboot and verify provider selection persists.
10. Uninstall with `adb uninstall com.aeiou.widgetbar`; confirm no launcher/system package was modified.

## Safety rollback

The widget is an ordinary user APK. Removal is a single package uninstall; no system files are changed.
