# Test plan

## CI gates

1. Source contract check: XML parse, zero requested permissions, one collapsed right-side ChatGPT icon, four search providers, native ChatGPT route, browser fallback, launcher-aware placement guard and no pin success callback.
2. Gradle 8.13 wrapper + AGP 8.13.2 dependency resolution.
3. compileSdk 36 debug build.
4. JVM unit tests for provider persistence mapping and LauncherPinPolicy behavior.
5. Android lint with abort-on-error.
6. APK identity, SDK, permission, ZIP-alignment and signature audit.
7. Debug APK artifact upload.

## Current verified evidence

- Native ChatGPT route: `chatgpt://` resolves through `com.openai.chatgpt/.ChatGptDeeplinkActivity` and lands in `.MainActivity` on ChatGPT Android 1.2026.251.
- UI dump after that native launch showed the fresh composer action `ChatGPT fragen` and no prior conversation content.
- The previous HTTPS app-link route was rejected because it ultimately resumed LineageOS Jelly instead of remaining in ChatGPT.
- The feature branch previously completed the full local JDK 17 / Gradle 8.13 gate: build, JVM tests, lint, contract and APK audit.
- The launcher widget picker does discover **Widget Bar**.
- On the affected Launcher3 device session, launcher-assisted placement produced bound widget IDs without a visible committed workspace widget.
- Final on-device verification of the new Launcher3 manual-placement path remains required.

## Device validation before normal use

1. Install the current branch APK with `adb install -r app-debug.apk`.
2. Launch setup with `adb shell am start -n com.aeiou.widgetbar/.SetupActivity`.
3. On default launcher `com.android.launcher3`: verify the setup button reads **Open home screen**, opens HOME, and shows manual placement instructions. Long-press an empty home-screen area → Widgets → search **Widget Bar** → drag it into place.
4. On another compatible launcher: request one automatic widget pin and verify the launcher completes placement without `SetupActivity` being relaunched as a success callback.
5. Verify exactly one new Widget Bar instance is visible on the workspace and that AppWidgetService reports the same provider/widget ID.
6. Verify the collapsed widget has one active provider icon left, search hint center, and exactly one ChatGPT icon right.
7. Tap the left provider icon: the drop-up must contain only the search field plus Google, YouTube, Instagram and TikTok icons.
8. Switch each provider and confirm the selected icon immediately becomes the persistent left widget icon.
9. Submit one query for each provider and verify the expected installed app or browser fallback opens.
10. Tap the right ChatGPT icon and verify the installed ChatGPT version lands on a fresh/new-chat composer.
11. Reboot and verify provider selection persists.
12. Remove only the test widget/app and confirm no launcher/system package was modified.

## Regression checks for placement

- Launcher3 must not receive an external automatic pin request from SetupActivity.
- Unsupported/rejected automatic pin requests must fall back to manual placement instructions.
- SetupActivity must not use a pin success PendingIntent.
- Manual placement must leave the launcher fully responsible for workspace selection and final widget placement.

## Safety rollback

The widget is an ordinary user APK. Removal is a single package uninstall; no system files are changed.
