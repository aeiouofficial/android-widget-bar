# Android Widget Bar

Standalone Android home-screen search/create widget.

## Visual contract

The widget always looks like one compact 48dp pill:

```text
[ ACTIVE APP ] [ SEARCH / CREATE FIELD ] [ APP SELECTOR ]
```

- left: exactly one active app icon
- center: current search/create field
- right: exactly one app-selector icon
- idle and edit mode use the same compact visual geometry
- selector opens vertically upward without overlapping the main bar

## Interaction contract

### Center writing bar

- **Single tap:** activates Widget Bar's own editable field and keyboard.
- **Double-tap:** opens the currently selected app normally, without running a search.
- **Submit:** only a non-empty query/prompt followed by the keyboard search/send action opens the selected provider with that content.
- Empty/whitespace submit does nothing.

Double-tap detection is handled by gesture-aware `SearchActivity`. During Android's standard double-tap window, the activity first allows very early second taps to pass through to the launcher so the same widget PendingIntent can be delivered again; once its window has focus, later second taps are captured directly. If no second tap arrives, the widget switches into the editable surface and opens the keyboard. A valid second tap opens the selected app normally instead of starting text entry.

### Right selector

Tap the right selector to choose:

- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

The selected app moves to the left and persists.

### ChatGPT left-icon quick actions

When ChatGPT is selected, tapping the **left ChatGPT icon** opens a compact adapted quick-action menu:

- voice conversation
- camera capture
- image upload/photo picker
- dictation/recording

These are implemented with supported Android contracts rather than copied internal ChatGPT widget code:

- voice: ChatGPT's verified `https://chatgpt.com/voice` app deep link
- camera: Android camera capture to an app-created MediaStore image, then targeted image share to ChatGPT
- photo: Android system photo picker/document fallback, then targeted image share to ChatGPT
- dictation: Android speech recognizer, then the recognized text is handed to the existing ChatGPT new-chat prompt flow

No additional runtime/system permissions are requested by Widget Bar.

## Keyboard behavior

The persistent launcher AppWidget uses RemoteViews, so free-form typing is handled by a transient permission-free `SearchActivity`.

It:

- visually matches the same compact pill
- keeps the selected app icon left and selector right
- uses IME resize and visible-frame tracking
- moves above the keyboard so typed text stays visible
- temporarily hides the launcher-owned widget to prevent a duplicate visible bar

## Provider behavior

- Google → search after explicit submit
- YouTube → search after explicit submit
- Instagram → keyword search after explicit submit
- TikTok → search after explicit submit
- ChatGPT → typed prompt handed to ChatGPT after explicit submit

## Safety

No root, ROM changes, launcher replacement, accessibility service, draw-over-other-apps permission, dangerous permissions, or launcher database mutation.

See `docs/ARCHITECTURE.md` and `docs/TEST_PLAN.md`.
