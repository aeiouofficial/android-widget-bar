# Android Widget Bar

Standalone Android home-screen search/create widget.

## Interaction contract

The widget is a single rounded pill:

```text
[ ACTIVE APP ] [ SEARCH / CREATE FIELD ] [ APP SELECTOR ]
```

- **Left:** exactly one active app icon.
- **Center:** the current action label.
- **Right:** exactly one neutral app-selector icon.
- Right-side selector options: Google, YouTube, Instagram, TikTok, ChatGPT.
- Selecting an app immediately moves that app to the left and persists the choice.
- The right side always remains the selector.

### Critical input rule

**Tapping the left icon or center field never launches Google, YouTube, Instagram, TikTok, or ChatGPT.**

A tap only activates Widget Bar's own editable composer. The provider app is opened **only after**:

1. the user types a non-empty query/prompt into the bar; and
2. explicitly presses the keyboard search/send action.

Empty submit does nothing.

### Search targets

- Google: type query → press search → Google search opens.
- YouTube: type query → press search → YouTube search opens.
- Instagram: type query → press search → Instagram keyword search opens.
- TikTok: type query → press search → TikTok search opens.
- ChatGPT: type the new-chat prompt → press send/search → only then is ChatGPT opened with the typed prompt handed to the app.

ChatGPT's collapsed label is **New chat**; its editable hint is **Ask ChatGPT…**.

## Keyboard behavior

The launcher-owned AppWidget itself is rendered with `RemoteViews`, which cannot host a normal free-form `EditText`.

Widget Bar therefore activates its own transient, permission-free `SearchActivity` that redraws the same pill with a real `EditText`:

```text
[ ACTIVE APP ] [ EDITABLE TEXT ] [ APP SELECTOR ]
```

It uses Android IME resize plus the visible-window frame to keep the active bar **above the on-screen keyboard**, so typed text remains visible. This surface does not launch the selected provider until explicit submit.

No overlay permission, accessibility service, root, launcher modification, or system mutation is used.

## App selector

The selector is a compact **icon-only** drop-up. It never contains a second search field.

Targets:

- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

## ChatGPT handoff

On non-empty submit only:

1. Widget Bar forwards the typed prompt to the installed ChatGPT app using Android's text-share intent contract.
2. If that route is unavailable, the native `chatgpt://` new-chat route remains as fallback.
3. Browser fallback is retained as a last resort.

A plain tap while ChatGPT is selected must never open ChatGPT.

## Safety

- no root
- no ROM changes
- no launcher replacement
- no accessibility service
- no draw-over-other-apps permission
- no dangerous permissions
- no launcher database mutation
- installed application icons are loaded at runtime

## Verification

GitHub Actions quota is exhausted. Build, unit tests, lint, contract checks, and APK audit are therefore executed locally with the repository Gradle wrapper and JDK 17.

See `docs/ARCHITECTURE.md` and `docs/TEST_PLAN.md`.
