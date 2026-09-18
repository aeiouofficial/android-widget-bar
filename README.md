# Android Widget Bar

Standalone Android home-screen search/create widget.

## Visual contract

The widget always looks like one compact typing/search pill:

```text
[ ACTIVE APP ] [ SEARCH / CREATE FIELD ] [ APP SELECTOR ]
```

There is no separate large idle-state container. The persistent launcher widget and the active editable surface use the same compact 48dp pill geometry, spacing, background, border, left active-app icon, center field, and right selector control.

## Interaction contract

- **Left:** exactly one active app icon.
- **Center:** the current search/create action.
- **Right:** exactly one neutral app-selector icon.
- Selector targets: Google / YouTube / Instagram / TikTok / ChatGPT.
- Selecting a target moves that app to the left and persists it.
- The right side always remains the selector.

### Selector behavior

Tapping the selector opens an **icon-only vertical drop-up**.

```text
            [ Google    ]
            [ YouTube   ]
            [ Instagram ]
            [ TikTok    ]
            [ ChatGPT   ]
[ ACTIVE ][ FIELD      ][ SELECTOR ]
```

The selector must be entirely above the bar with a visible gap. It must never expand horizontally to the left and must never overlap the main bar.

### Input behavior

Tapping the left icon or center field never opens the provider directly.

A tap only activates Widget Bar's own editable field. The provider opens only after:
1. non-empty text has been entered; and
2. the user presses the keyboard search/send action.

Blank/whitespace submit does nothing.

### Keyboard behavior

The editable surface:
- visually matches the persistent compact pill;
- keeps the selected app icon on the left;
- keeps the selector on the right;
- uses `SOFT_INPUT_ADJUST_RESIZE`;
- tracks the visible display frame;
- moves above the keyboard when needed so typed text is always visible.

The launcher-owned widget is temporarily hidden while this active surface is shown, preventing a duplicate visible bar.

## Provider behavior

- Google → query submit opens Google search.
- YouTube → query submit opens YouTube search.
- Instagram → query submit opens Instagram keyword search.
- TikTok → query submit opens TikTok search.
- ChatGPT → collapsed label **New chat**; editable hint **Ask ChatGPT…**; only a submitted non-empty prompt opens ChatGPT and forwards the typed text.

## Safety

No root, ROM changes, launcher replacement, accessibility service, draw-over-other-apps permission, dangerous permissions, or launcher database mutation.

See `docs/ARCHITECTURE.md` and `docs/TEST_PLAN.md`.
