# Android Widget Bar

Standalone Android home-screen search widget.

## Current design

- Concept D minimal pill: no internal divider lines.
- Left: active provider icon.
- Center: active provider search hint.
- Right: exactly one ChatGPT icon.
- Tap left icon: compact drop-up with only the search field and Google / YouTube / Instagram / TikTok icons.
- Tap center: same drop-up with search focused.
- Pick a provider: its icon becomes the persistent active icon on the left.
- Tap ChatGPT: open the ChatGPT root/new-chat route; browser fallback if the installed app does not claim it.

## Safety

No root. No ROM changes. No launcher replacement. No accessibility service. No overlay permission. No dangerous permissions. No bundled third-party brand artwork; installed application icons are loaded at runtime.

## Build

Toolchain:

- JDK 17
- Gradle 8.13 (project wrapper)
- Android Gradle Plugin 8.13.2
- compileSdk / targetSdk 36
- minSdk 26

GitHub Actions builds and lints the APK on every feature-branch push using the checked-in Gradle wrapper.

## ADB installation

After downloading the CI artifact:

```powershell
adb install -r app-debug.apk
adb shell am start -n com.aeiou.widgetbar/.SetupActivity
```

Use the Android pin-widget confirmation or add **Widget Bar** from the launcher's widget picker.

Rollback:

```powershell
adb uninstall com.aeiou.widgetbar
```

See `docs/ARCHITECTURE.md` and `docs/TEST_PLAN.md`.
