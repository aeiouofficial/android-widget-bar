# Test plan

## Local gates

1. XML parses.
2. No requested permissions.
3. Collapsed layout contains:
   - one active app icon;
   - one center action field;
   - one selector icon.
4. Collapsed layout uses a dedicated fixed 48dp `widget_pill` centered inside a transparent AppWidget root.
5. Edit mode uses the same compact pill geometry.
6. Selector is:
   - vertical;
   - icon-only;
   - Google / YouTube / Instagram / TikTok / ChatGPT;
   - fully above the bar;
   - non-overlapping.
7. Plain tap opens only the local editable surface.
8. Empty submit cannot launch a provider.
9. ChatGPT cannot open before prompt submit.
10. Keyboard-aware edit surface uses resize + visible-frame repositioning.
11. Gradle debug/release builds.
12. Debug/release unit tests.
13. Debug/release lint.
14. APK audit and `git diff --check`.

## Device validation

### Idle appearance

- Persistent bar must visually match the compact edit-mode bar.
- No large/tall background block around the idle widget.
- Left icon, center text, and right selector must have the same spacing as edit mode.

### Selector

1. Tap right selector.
2. Verify five icons form one vertical column.
3. Verify the column grows upward.
4. Verify there is a visible gap between the list and the main bar.
5. Verify the list does not extend horizontally over the bar.
6. Pick each target and verify it moves left.

### Input

1. Tap left or center.
2. Verify provider app does not open.
3. Verify active bar remains compact.
4. Verify keyboard appears.
5. Verify bar moves above keyboard if necessary.
6. Type text and verify it remains visible.
7. Press search/send.
8. Only then verify selected provider opens.

### ChatGPT

1. Select ChatGPT.
2. Verify left icon = ChatGPT, center = **New chat**.
3. Tap bar: ChatGPT must remain closed.
4. Type prompt.
5. Submit.
6. Only then verify ChatGPT opens with the prompt handoff.

### Persistence

Reboot and confirm selected target remains selected.
