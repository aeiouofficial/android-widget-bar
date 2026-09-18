# Test plan

## Local gates

1. XML parse.
2. Zero requested permissions.
3. Persistent pill remains compact 48dp.
4. Right selector remains vertical, icon-only, and non-overlapping.
5. Center writing-bar tap launches gesture-aware `SearchActivity` directly.
6. Double-tap is captured in the foreground inside the original writing-bar bounds and opens the selected app normally.
7. Single tap opens only the local editable surface.
8. Explicit non-empty submit is still required for provider search/create handoff.
9. ChatGPT left icon exposes voice / camera / photo / dictation actions.
10. Voice uses ChatGPT voice deep link.
11. Camera uses system capture + targeted image share.
12. Photo uses system picker + targeted image share.
13. Dictation uses speech recognizer + ChatGPT prompt handoff.
14. Debug build.
15. JVM tests.
16. Lint.
17. APK audit.
18. `git diff --check`.

## Device validation

### Center writing bar

For each selected target:

1. single tap center once
2. verify provider app does not open
3. after the double-tap timeout, verify local edit field appears
4. verify keyboard appears and field stays visible above it
5. cancel and return home
6. double-tap center within Android double-tap timing
7. verify the selected app opens to its normal home, not to search results

### Search submit

For each search provider:

1. single tap
2. type unique query
3. press search/send
4. verify only then the selected provider opens with the query

For ChatGPT:

1. single tap center
2. type prompt
3. submit
4. verify only then ChatGPT opens with prompt handoff

### ChatGPT left icon

With ChatGPT selected:

1. tap the left ChatGPT icon
2. verify a compact quick-action menu appears above the icon
3. verify actions: voice, camera, photo upload, dictation
4. voice → ChatGPT voice mode
5. camera → camera UI → captured image handed to ChatGPT
6. photo → Android photo picker → chosen image handed to ChatGPT
7. dictation → speech recognizer → recognized text handed to ChatGPT
8. verify Widget Bar itself requested no new runtime permissions

### Persistence

Reboot and confirm selected provider remains selected.
