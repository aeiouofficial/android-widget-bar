# Test plan

## Local gates

1. XML parse.
2. Zero requested permissions.
3. Persistent pill remains compact 48dp.
4. Right provider selector remains vertical, icon-only, and non-overlapping.
5. Left mode picker remains vertical, provider-scoped, and non-overlapping.
6. Mode selection is persisted independently per provider.
7. Center retains Android-standard double-tap disambiguation.
8. Double-tap opens the selected app normally.
9. Text modes require non-empty explicit submit.
10. Non-text action modes launch immediately from the left mode picker; no additional center-bar tap is allowed.
11. Google Gemini uses `PROCESS_TEXT` with the typed prompt.
12. YouTube Shorts/subscriptions use installed-app shortcut actions.
13. Instagram Story/Reel/Messages routes remain present.
14. TikTok Create/Inbox routes remain present.
15. ChatGPT Voice/Camera/Photo/Dictation remain available through `ChatGptMediaActivity`.
16. Debug build.
17. JVM tests.
18. Lint.
19. APK audit.
20. `git diff --check`.

## Device validation

### Provider selector

For Google / YouTube / Instagram / TikTok / ChatGPT:

1. open the right selector
2. choose provider
3. verify selected app icon moves left
4. verify that provider's persisted mode center label appears

### Left mode picker

For each provider:

1. tap left app icon
2. verify only that provider's modes are shown
3. verify vertical upward layout with no overlap
4. choose a mode
5. verify center label updates
6. reopen picker and verify selected mode highlight/persistence

Expected mode counts:

- Google: 2
- YouTube: 3
- Instagram: 4
- TikTok: 3
- ChatGPT: 5

### Text modes

For Google Search, Google Gemini, YouTube Search, Instagram Search, TikTok Search, ChatGPT New chat:

1. select mode
2. single tap center
3. verify provider app does not open before submit
4. verify keyboard appears
5. verify active bar is visible above keyboard
6. type unique text
7. press search/send
8. verify selected destination receives/uses the text

Gemini-specific:
- verify typed text appears verbatim in Gemini after submit

### Action modes

For each action mode:

1. select mode from the left picker
2. verify center label changes
3. single tap center
4. wait past double-tap timeout
5. verify expected app surface opens

Required:

- YouTube Shorts
- YouTube Subscriptions
- Instagram Story
- Instagram Reel
- Instagram Messages
- TikTok Create
- TikTok Inbox
- ChatGPT Voice
- ChatGPT Camera
- ChatGPT Photo
- ChatGPT Dictation

### Double-tap regression

For at least one provider in each mode class:

1. double-tap center inside Android timing
2. verify selected app's normal home opens instead of the selected mode

### Persistence

1. choose a non-default mode for several providers
2. switch providers
3. switch back
4. verify each provider restores its own mode
5. reboot
6. verify provider and mode persistence

### Safety

Confirm:

- zero Widget Bar permissions
- no launcher/system package mutation
- no accessibility/overlay service
- no root/system modifications
