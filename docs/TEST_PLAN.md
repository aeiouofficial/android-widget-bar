# Test plan

## Local code gates

Run locally because GitHub Actions quota is exhausted.

1. Contract check:
   - XML valid;
   - zero requested permissions;
   - collapsed widget has exactly active icon + center label + selector;
   - selector targets Google / YouTube / Instagram / TikTok / ChatGPT;
   - selector contains no `EditText`;
   - left/center always opens only `SearchActivity`;
   - collapsed widget never directly references `ChatGptNewChatActivity`;
   - SearchActivity has no ChatGPT auto-launch branch;
   - blank input is rejected before all provider branches;
   - ChatGPT typed prompt is forwarded only after submit;
   - IME resize and visible-frame repositioning are present.
2. `:app:assembleDebug`.
3. JVM unit tests.
4. `:app:lintDebug`.
5. APK package / SDK / permission / alignment / signature audit.
6. `git diff --check`.

## Device interaction gates

### Selection

1. Add one Widget Bar instance.
2. Tap selector.
3. Verify icon-only drop-up: Google / YouTube / Instagram / TikTok / ChatGPT.
4. Pick each target.
5. Verify selected target moves to the left and persists.

### No premature launch

For **every target**, including ChatGPT:

1. tap left icon or center field;
2. verify Widget Bar's editable surface opens;
3. verify provider app has **not** opened;
4. verify keyboard is shown;
5. verify the editable bar is visible above the keyboard;
6. type text and verify the text remains visible;
7. press Back/cancel and verify provider app still never opened.

### Empty submit

For every target:

1. activate editable surface;
2. submit with empty/whitespace input;
3. verify no provider app opens and the editable surface remains active.

### Explicit submit

For Google, YouTube, Instagram, TikTok:

1. type a unique query;
2. press keyboard search/send;
3. only then verify the selected provider opens with that query.

For ChatGPT:

1. select ChatGPT;
2. verify left icon is ChatGPT and collapsed center says **New chat**;
3. tap field;
4. verify ChatGPT does not open;
5. type a unique prompt;
6. press keyboard search/send;
7. only then verify ChatGPT opens and receives the typed prompt/new-chat handoff.

### Keyboard

Test the widget in a low home-screen position:

1. activate input;
2. verify IME appears;
3. verify active bar is repositioned above the IME;
4. verify typed text and selector remain visible.

### Persistence

1. choose a provider;
2. reboot;
3. verify same provider remains selected;
4. verify tap-vs-submit behavior is unchanged.

## Safety

Confirm no launcher/system package mutation and no additional permissions.
