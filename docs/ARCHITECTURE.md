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
- SetupActivity: optional launcher-assisted widget pin flow.
- WidgetPrefs: persistent selected-provider state.
- IconLoader: uses installed app icons at runtime; no copied brand assets are bundled.

## Widget pin ownership

`SetupActivity` only requests the pin through `AppWidgetManager.requestPinAppWidget`. It deliberately passes no success callback. The launcher owns confirmation, workspace selection and final placement. This avoids the observed failure mode where a success callback relaunched `SetupActivity` while the launcher still had a pending widget placement with no committed screen/cell.

The ordinary launcher widget picker remains a supported alternative and must continue to discover **Widget Bar** through the exported `SearchBarWidgetProvider` metadata.

## Security properties

The app requests no dangerous permissions, no root, no accessibility service, no draw-over-other-apps permission, and no network permission. Search navigation is delegated to installed apps or the user's browser through Android intents.
