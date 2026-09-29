# Test plan

## Local gates

1. XML parse.
2. Zero requested permissions.
3. Persistent pill remains compact 48dp.
4. Classic right provider selector remains vertical, icon-only, and non-overlapping.
5. Wheel variant uses an infinite vertical `ListView` slot reel and contains no `StackView`, rotation/card stack, or discrete cycle policy.
6. Left mode picker remains vertical, provider-scoped, and non-overlapping.
7. Mode selection is persisted independently per provider.
8. Center retains Android-standard double-tap disambiguation.
9. Double-tap opens the selected app normally.
10. Text modes require non-empty explicit submit.
11. Non-text action modes launch immediately from the left mode picker; no additional center-bar tap is allowed.
12. Google Gemini uses `PROCESS_TEXT` with the typed prompt.
13. YouTube Shorts/subscriptions use installed-app shortcut actions.
14. Instagram Story/Reel/Messages routes remain present.
15. TikTok Create/Inbox routes remain present.
16. ChatGPT Voice/Camera/Photo/Dictation remain available through `ChatGptMediaActivity`.
17. Classic and Wheel AppWidget receivers are non-exported; Wheel `RemoteViewsService` remains protected by `BIND_REMOTEVIEWS`.
18. Classic/Wheel PendingIntents contain instance-unique data identities and center actions forward `EXTRA_APPWIDGET_ID`.
19. Edit visibility is scoped to the exact source `appWidgetId`.
20. External app/deep-link starts are guarded and retain fallbacks when the target activity cannot launch.
21. ChatGPT media activity saves pending camera state across recreation and cleans failed pending rows.
22. Debug build.
23. JVM tests.
24. Lint.
25. APK audit.
26. `git diff --check`.

## Device validation

### Provider selectors

**Classic** — for Google / YouTube / Instagram / TikTok / ChatGPT:

1. open the right selector
2. choose provider
3. verify selected app icon moves left
4. verify that provider's persisted mode center label appears

**Wheel**:

1. add the separate `Widget Bar - Wheel` widget
2. swipe vertically inside only the right-side 44dp provider reel
3. swipe past at least two full five-provider cycles upward and downward; verify icons repeat with no terminal end or overscroll edge
4. tap the visible provider icon
5. verify that provider becomes active and its mode menu opens on the right in the same interaction
6. verify the center label updates after the provider is committed
7. repeat inside the editable `SearchActivity` Wheel variant

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

1. open the provider's mode picker
2. tap the action-mode icon
3. verify the expected app surface opens immediately
4. verify no additional center-bar tap is required

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

### Multi-instance isolation

1. Place at least two Classic widgets and one Wheel widget.
2. Tap the center of one Classic widget and wait for edit mode.
3. Verify only that exact widget is hidden; the other Classic and Wheel instances stay visible.
4. Exit edit mode and verify the source widget returns.
5. Repeat from the Wheel widget.
6. Alternate rapidly between Classic and Wheel center/left/right actions and verify variant/provider extras never cross over.

### Failure fallback

1. Disable or uninstall one optional target app.
2. Trigger its search/action route.
3. Verify Widget Bar falls back without crashing.
4. Repeat with ChatGPT absent for New chat, Voice, Camera/Photo share.
5. Confirm Logcat contains route/cause metadata but no entered query text.

### Media recreation

1. Start ChatGPT Camera from Widget Bar.
2. Background/rotate/recreate Widget Bar while the camera contract is active.
3. Complete the capture.
4. Verify a single capture is published and shared; no second camera launch occurs.
5. Cancel a capture and verify no pending WidgetBar image remains visible.

### Safety

Confirm:

- zero Widget Bar permissions
- Classic/Wheel AppWidget receivers are non-exported
- Wheel collection service is protected by `BIND_REMOTEVIEWS`
- no launcher/system package mutation
- no accessibility/overlay service
- no root/system modifications
