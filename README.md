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
- Tap ChatGPT: open the native `chatgpt://` route in `com.openai.chatgpt`; browser fallback to chatgpt.com if the installed app does not resolve it.

## Safety

No root. No ROM changes. No launcher replacement. No accessibility service. No overlay permission. No dangerous permissions. No bundled third-party brand artwork; installed application icons are loaded at runtime.

## Build

Toolchain:

- JDK 17
- Gradle 8.13 (project wrapper)
- Android Gradle Plugin 8.13.2
- compileSdk / targetSdk 36
- minSdk 26

The branch is configured for GitHub Actions build, unit-test, lint, contract and APK audit gates. GitHub-hosted execution is currently account-blocked before runner startup by the repository owner's Actions billing/spending state; local JDK 17 / Gradle 8.13 verification remains the accepted evidence until that external block is cleared.

## Widget placement

The setup screen uses two safe placement paths:

- Default launcher `com.android.launcher3` (AOSP/Lineage Launcher3): open the home screen and place **Widget Bar** manually from the launcher widget picker.
- Other launchers: use Android's standard `requestPinAppWidget` flow. If that API is unsupported or rejects the request, setup falls back to the same manual picker instructions.

No code writes to launcher databases or modifies launcher/system packages.

## ADB installation

After building or downloading the debug APK:

```powershell
adb install -r app-debug.apk
adb shell am start -n com.aeiou.widgetbar/.SetupActivity
```

On AOSP/Lineage Launcher3, tap **Open home screen**, then long-press an empty home-screen area → **Widgets** → search **Widget Bar** → drag it into place.

Rollback:

```powershell
adb uninstall com.aeiou.widgetbar
```

See `docs/ARCHITECTURE.md` and `docs/TEST_PLAN.md`.
