# Architecture

## Stable baseline

The verified pre-mode-expansion snapshot is preserved at tag/release `v0.1.0-working`.

Mode expansion work continues on `feat/app-mode-actions-v0.2`.

## Persistent widget

The launcher-owned AppWidget is a transparent host containing a fixed 48dp pill:

```text
[ active app ][ current mode / input ][ app selector ]
```

`SearchBarWidgetProvider` loads:

1. active `ProviderTarget`
2. that provider's persisted `ProviderMode`
3. active app runtime icon
4. the mode's collapsed center label

Click wiring:

- left icon → `ModePickerActivity`
- center → gesture-aware `SearchActivity`
- right icon → `PickerActivity`

## Provider and mode state

`ProviderTarget` models the five apps.

`ProviderMode` models provider-scoped modes and classifies each as either:

- **text mode** — requires Widget Bar text entry and explicit submit
- **action mode** — runs after the single-tap/double-tap timing window

`WidgetPrefs` persists:

- one active provider
- one last-selected mode per provider

Defaults:

- Google Search
- YouTube Search
- Instagram Search
- TikTok Search
- ChatGPT New chat

## App selector

`PickerActivity` is the existing icon-only vertical drop-up on the right.

Selecting an app updates the active provider. Its previously selected mode is restored automatically; if no mode was stored, its default is used.

## Mode selector

`ModePickerActivity` is a compact vertical drop-up anchored above the left app icon.

It calls `ProviderMode.modesFor(provider)`, so only relevant modes appear. Selecting a text mode persists it, updates all widget instances, and closes the picker. Selecting a non-text action mode does the same and then immediately calls `SearchLauncher.launchAction(...)` before closing, so there is no redundant second center-bar tap.

Current mode sets:

- Google: Search, Gemini
- YouTube: Search, Shorts, Subscriptions
- Instagram: Search, Story, Reel, Messages
- TikTok: Search, Create, Inbox
- ChatGPT: New chat, Voice, Camera, Photo, Dictation

## Center interaction

`SearchActivity` owns single/double-tap disambiguation.

### Double-tap

A valid Android-timed double-tap calls `SearchLauncher.openAppHome(...)`, which prefers the selected package's launcher intent.

### Text mode single tap

After the double-tap timeout:

1. launcher widget is hidden
2. matching 48dp editable pill is shown
3. keyboard is opened
4. bar is moved above the IME if required
5. explicit non-empty submit calls `SearchLauncher.launch(..., ProviderMode, query)`

### Action mode single tap

After the same double-tap timeout, no keyboard is shown. `SearchLauncher.launchAction(...)` runs the active action mode.

## Mode routes

### Google

**Search** uses normal Google web-search routing.

**Gemini** uses:

```text
ACTION_PROCESS_TEXT
type = text/plain
EXTRA_PROCESS_TEXT = typed Widget Bar text
EXTRA_PROCESS_TEXT_READONLY = true
package = com.google.android.googlequicksearchbox
```

This route was device-verified to open Gemini and prefill the exact query. It is preferable to Gemini URL query parameters, which opened Gemini but did not prefill the prompt on the test device.

### YouTube

Widget Bar uses the installed YouTube app's own launcher shortcut actions discovered from `cmd shortcut get-shortcuts`:

- Shorts → `com.google.android.youtube.action.open.shorts`
- Subscriptions → `com.google.android.youtube.action.open.subscriptions`

Web URL fallbacks remain available.

### Instagram

Installed Instagram shortcuts/deep links verified on the test device:

- Story → `instagram://story-camera`
- Reel → `instagram://reels-camera`
- Messages → `instagram://direct-inbox`

Instagram's externally exposed shortcut set does not provide a stable arbitrary-user Direct-thread route. Widget Bar therefore opens the Direct inbox rather than fabricating an unsupported username-to-thread contract.

### TikTok

Resolved installed-app deep links:

- Create → `snssdk1233://aweme/create`
- Inbox → `snssdk1233://aweme/notification`

On the current test device TikTok is not signed in, so those routes may land on TikTok's login flow until the user authenticates.

### ChatGPT

ChatGPT mode actions reuse `ChatGptMediaActivity`:

- Voice
- Camera
- Photo
- Dictation

New-chat text submit retains its targeted text-share/native/browser fallback chain.

## Keyboard handling

For text modes `SearchActivity`:

- uses `SOFT_INPUT_ADJUST_RESIZE`
- observes `getWindowVisibleDisplayFrame(...)`
- repositions the 48dp pill above the IME
- hides the launcher widget during actual editing
- restores it on pause/finish
- allows the left mode picker and right app picker from edit mode

## Safety

The app requests zero permissions and does not modify launcher databases, system packages, ROM state, or accessibility/overlay settings.
