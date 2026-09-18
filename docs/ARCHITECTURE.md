# Architecture

## Visual authority

Persistent widget and edit mode are two renderings of the same compact pill:

```text
[ active app ][ center action/input ][ selector ]
```

The launcher host can allocate more vertical cell space than the visual bar needs. Therefore the AppWidget root remains transparent and contains a dedicated fixed-height 48dp `widget_pill` child centered vertically. This prevents the idle widget background from expanding to the full launcher cell height.

`SearchActivity` reproduces the same 48dp pill geometry for real text input.

## Selector

`PickerActivity` is a translucent icon-only selector.

It is strictly vertical:

```text
Google
YouTube
Instagram
TikTok
ChatGPT
```

The list is positioned completely above the selector anchor with a gap. It never renders as a horizontal row and never overlaps the main widget bar.

Launcher3 does not reliably propagate `sourceBounds` for RemoteViews PendingIntents. When source bounds are unavailable, `PickerActivity` uses a conservative right-edge fallback positioned above the normal widget area.

When the selector is opened from `SearchActivity`, it anchors to the original widget source bounds when available, not to the keyboard-shifted edit bar.

## Tap vs submit

A plain tap on the active app icon or center area opens only `SearchActivity`.

No provider app is launched at tap time.

Only a non-empty explicit IME search/send action can call `SearchLauncher.launch(...)`.

## Keyboard handling

`SearchActivity`:
- uses `SOFT_INPUT_ADJUST_RESIZE`;
- observes `getWindowVisibleDisplayFrame(...)`;
- repositions the compact bar above the IME;
- hides the launcher-owned RemoteViews widget while editing;
- restores it on pause/finish;
- uses zero transition animation.

## Provider state

`WidgetPrefs` persists the selected `ProviderTarget`.

Targets:
- Google
- YouTube
- Instagram
- TikTok
- ChatGPT

ChatGPT follows the same edit-first rule as every other provider.

## ChatGPT handoff

After a non-empty prompt submit:
- targeted Android text-share intent to `com.openai.chatgpt`;
- native `chatgpt://` fallback;
- browser fallback as last resort.

## Launcher safety

Launcher3 uses normal manual widget placement. Other compatible launchers may use standard `requestPinAppWidget`.

No launcher database/workspace/system mutation is performed.
