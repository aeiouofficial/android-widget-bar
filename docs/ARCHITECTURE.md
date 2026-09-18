# Architecture

## Goal

A standalone Android home-screen widget that replaces the stock search-bar workflow without modifying the launcher or any system package.

## Interaction contract

- The left icon is always the currently selected search provider.
- The middle text is the active provider's search hint.
- The right side contains exactly one icon: ChatGPT.
- Tapping the active provider icon opens a compact drop-up containing only the search field and provider icons.
- Tapping the search hint opens the same drop-up with the search field focused.
- Selecting Google, YouTube, Instagram, or TikTok updates the left icon immediately.
- Submitting the search opens the selected provider.
- Tapping ChatGPT opens the native `chatgpt://` deep link in `com.openai.chatgpt`, which was device-verified against ChatGPT Android 1.2026.251 to land on a fresh empty composer. If the installed app does not resolve that native route, the browser fallback opens chatgpt.com.

## Components

- SearchBarWidgetProvider: RemoteViews widget renderer and click wiring.
- PickerActivity: translucent, permission-free drop-up UI anchored above the clicked widget using Intent source bounds.
- SearchLauncher: provider-specific search routing with browser fallback.
- ChatGptNewChatActivity: isolated ChatGPT new-chat/home route.
- SetupActivity: safe launcher-aware widget placement entry point.
- LauncherPinPolicy: isolates the default-launcher compatibility decision for unit testing.
- WidgetPrefs: persistent selected-provider state.
- IconLoader: uses installed app icons at runtime; no copied brand assets are bundled.

## Widget placement compatibility

The default HOME package is resolved at setup time. For `com.android.launcher3` (AOSP/Lineage Launcher3), setup deliberately avoids the external automatic pin request and opens the home screen with explicit manual widget-picker instructions.

This is a conservative compatibility path based on the affected device session, where launcher-assisted placement produced bound widget IDs without a visible committed workspace widget. The ordinary launcher widget picker did discover **Widget Bar**, so the provider remains available through the launcher-owned placement UI.

Other launchers keep the standard `AppWidgetManager.requestPinAppWidget(provider, null, null)` flow. No success callback is supplied, so confirmation and workspace placement stay launcher-owned. If automatic pinning is unsupported or the request is rejected, setup falls back to the manual picker path.

The app never edits launcher databases, workspace files, or system settings.

## Security properties

The app requests no dangerous permissions, no root, no accessibility service, no draw-over-other-apps permission, and no network permission. Search navigation is delegated to installed apps or the user's browser through Android intents.
