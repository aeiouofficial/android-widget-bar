# Android Widget Bar

Standalone Android home-screen search/create widget.

## Visual contract

The widget is always one compact 48dp pill:

```text
[ ACTIVE APP ] [ CURRENT MODE / INPUT ] [ APP SELECTOR ]
```

- left: active app icon
- center: current mode label or editable field
- right: app selector
- idle and edit mode share the same pill geometry
- **Classic** keeps the original tap-to-open provider picker
- **Wheel** replaces the old rotating/stack selector with an infinite vertical app slot reel inside the right-side slot
- left mode menus remain vertical and non-overlapping

## Controls

### Right side: choose app

Both variants expose the same providers:

- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

**Classic**: tap the right selector and choose from the existing vertical provider menu.

**Wheel**: the right side is an infinite vertical `ListView` slot reel, not a `StackView` and not a rotating card animation. Swipe vertically in either direction with no terminal end; the five providers repeat as a slot reel. Tap the visible provider icon to commit that provider and immediately open its provider-specific mode menu on the right.

The selected app becomes the left icon and its last selected mode is restored. Each app starts with its primary search/new-chat mode by default.

### Left app icon: choose mode

The left icon now opens a provider-scoped mode menu for **every** app.

**Google**
- Search
- Gemini

**YouTube**
- Search
- Shorts
- Subscriptions

**Instagram**
- Search
- Story
- Reel
- Messages

**TikTok**
- Search
- Create
- Inbox

**ChatGPT**
- New chat
- Voice
- Camera
- Photo
- Dictation

Mode choice is persisted independently per provider. Text modes stay selected for typing; non-text modes such as Instagram Story/Reel/Messages, YouTube Shorts, TikTok Create, or ChatGPT Camera execute immediately when their icon is chosen.

### Center bar

- **Single tap, text mode:** after double-tap disambiguation, opens Widget Bar's editable field and keyboard.
- **Selecting an action mode from the left menu:** launches it immediately. No extra center-bar tap is required.
- **Double-tap:** opens the selected app normally, regardless of mode.
- **Submit:** text modes open the selected destination only after non-empty text is entered and the keyboard search/send action is pressed.

## Verified mode routes

- Google Search → Google web search.
- Google Gemini → Android `PROCESS_TEXT` targeted to the installed Google app. On the test device this opens Gemini and pre-fills the exact text typed into Widget Bar.
- YouTube Shorts → YouTube's installed launcher shortcut action `com.google.android.youtube.action.open.shorts`.
- YouTube Subscriptions → installed launcher shortcut action `com.google.android.youtube.action.open.subscriptions`.
- Instagram Story → `instagram://story-camera`.
- Instagram Reel → `instagram://reels-camera`.
- Instagram Messages → `instagram://direct-inbox`.
- TikTok Create → `snssdk1233://aweme/create`.
- TikTok Inbox → `snssdk1233://aweme/notification`.
- ChatGPT Voice / Camera / Photo / Dictation → existing verified Widget Bar media routes.

Instagram does not expose a stable external shortcut that can address an arbitrary friend by username. The reliable mode therefore opens Direct Messages; a specific conversation can then be chosen in Instagram.

## Keyboard behavior

Text modes use a transient permission-free `SearchActivity` because Android launcher `RemoteViews` cannot host a normal free-form `EditText`.

It:

- visually matches the same compact pill
- keeps the active app icon left and app selector right
- supports the same left mode picker while editing
- uses IME resize and visible-window tracking
- moves above the keyboard so typed text remains visible
- temporarily hides the launcher-owned widget so there is never a duplicate visible bar

## Double-tap behavior

The center uses a gesture-aware `SearchActivity`.

- very early second taps pass through the temporarily non-touchable transparent activity and re-trigger the same widget PendingIntent
- once the activity has focus, later second taps in the Android double-tap window are captured directly
- a valid double-tap opens the selected app's normal launcher activity
- if the window expires with only one tap, the selected text/action mode starts

## Safety

No root, ROM changes, launcher replacement, accessibility service, draw-over-other-apps permission, dangerous permissions, or launcher database mutation.

The stable snapshot before this mode expansion is preserved as GitHub prerelease **v0.1.0-working**. The Classic v0.2 interaction remains available separately while the Wheel variant is developed as a second widget provider.

See `docs/ARCHITECTURE.md`, `docs/MODE_ROUTES.md`, and `docs/TEST_PLAN.md`.
