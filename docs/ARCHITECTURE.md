# Architecture

## UI model

Persistent launcher widget:

```text
[ active app ] [ action label ] [ selector ]
```

Editable active surface:

```text
[ active app ] [ EditText ] [ selector ]
```

The active surface visually replaces the interaction role of the launcher widget while input is in progress; it is not a second provider search widget.

## State

`WidgetPrefs` stores one `ProviderTarget`:

- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

Selection changes:

1. user taps the right selector;
2. `PickerActivity` shows only app icons;
3. user selects target;
4. target is persisted;
5. all Widget Bar instances update;
6. selected app appears on the left.

## Tap vs submit

This distinction is mandatory.

### Tap

Left icon or center area:

- opens only `SearchActivity`;
- focuses the local `EditText`;
- shows the IME;
- does **not** open the selected provider.

### Submit

Only a non-empty IME search/send action calls `SearchLauncher.launch(...)`.

`SearchLauncher` rejects null/blank input before any provider-specific branch. Therefore no provider app can open from an empty submit or a plain tap.

## SearchActivity

`SearchActivity` is translucent and permission-free.

Responsibilities:

- render active app icon;
- render a real editable center field;
- preserve selector on the right;
- request IME;
- use `SOFT_INPUT_ADJUST_RESIZE`;
- observe `getWindowVisibleDisplayFrame(...)`;
- move the bar above the keyboard when the original launcher position would be occluded;
- call `SearchLauncher` only from explicit non-empty submit.

ChatGPT is handled by the same editable surface as all search providers. Selecting ChatGPT never bypasses the input step.

## Provider routing

- Google: `ACTION_WEB_SEARCH`, URL fallback.
- YouTube: targeted search URL, browser fallback.
- Instagram: targeted keyword-search URL, browser fallback.
- TikTok: targeted search URL, browser fallback.
- ChatGPT: typed prompt is passed to `ChatGptNewChatActivity` only after submit.

## ChatGPT handoff

For a non-empty prompt:

- primary: targeted Android `ACTION_SEND` with `text/plain` and `Intent.EXTRA_TEXT` to `com.openai.chatgpt`;
- native fallback: `chatgpt://`;
- final fallback: `https://chatgpt.com/?q=...`.

This preserves the invariant that the ChatGPT app is not opened until the user has entered a prompt and submitted it.

## Selector

`PickerActivity` is a translucent icon-only selector. It contains no `EditText`, no duplicate search bar, and no provider action. Its only responsibility is target selection.

## Launcher safety

Launcher3 uses manual widget placement. Other compatible launchers use `requestPinAppWidget(provider, null, null)`.

The app does not edit launcher databases, workspace files, packages, or system settings.
