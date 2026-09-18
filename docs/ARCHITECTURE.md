# Architecture

## Persistent widget

The launcher-owned AppWidget is a transparent host containing one fixed 48dp pill:

```text
[ active app ][ center action ][ selector ]
```

The idle widget and the editable surface intentionally share the same visual geometry.

## App selection

`PickerActivity` is an icon-only vertical drop-up above the right selector.

Targets:

- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

`WidgetPrefs` persists the selected target and all Widget Bar instances refresh after selection.

## Single tap vs double-tap

The center writing bar launches `SearchActivity` directly with a gesture-aware flag.

`SearchActivity` immediately replaces the launcher-owned pill with the visually identical foreground pill, but deliberately waits through Android's standard `ViewConfiguration.getDoubleTapTimeout()` before focusing the field and showing the IME.

Flow:

1. first tap opens gesture-aware `SearchActivity`
2. the original writing-bar bounds are retained from the launcher source bounds
3. if a second physical tap lands inside those bounds before the timeout, `SearchLauncher.openAppHome(...)` opens the selected app normally
4. if no second tap arrives, the local `EditText` is focused and the keyboard opens

This avoids a background-activity-start race while still allowing the second physical tap to be captured reliably in the foreground. `TapGesturePolicy` contains the pure timing rule and is JVM-tested.

## Editable search surface

`SearchActivity`:

- renders a real `EditText`
- uses `SOFT_INPUT_ADJUST_RESIZE`
- tracks `getWindowVisibleDisplayFrame(...)`
- moves above the IME when necessary
- hides the launcher AppWidget during editing
- restores it on pause/finish
- calls `SearchLauncher.launch(...)` only from explicit non-empty submit

## Normal app opening

`SearchLauncher.openAppHome(...)` prefers the selected app's installed launcher intent. If unavailable it falls back to that service's normal web home.

This route is only for the center-bar double-tap gesture.

## ChatGPT left-icon actions

When ChatGPT is selected, the left icon opens `ChatGptActionsActivity` instead of the normal tap router.

The quick-action menu is vertical and anchored above the tapped icon when launcher source bounds are available.

Actions:

- **Voice:** `https://chatgpt.com/voice` targeted to `com.openai.chatgpt`
- **Camera:** system camera capture to a MediaStore URI, then `ACTION_SEND image/*` targeted to ChatGPT
- **Photo:** system photo picker / document fallback, then targeted `ACTION_SEND image/*`
- **Dictation:** Android speech recognition, then recognized text is forwarded through `ChatGptNewChatActivity`

This preserves the user's requested original-widget capabilities without depending on ChatGPT's private internal widget implementation.

## ChatGPT text handoff

Normal typed ChatGPT submit remains:

- targeted Android text share to `com.openai.chatgpt`
- native `chatgpt://` fallback
- web fallback

## Safety

The app requests no dangerous permissions and does not modify launcher/system state.
