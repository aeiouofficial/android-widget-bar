# Provider mode route matrix

This file records the route authority for `feat/app-mode-actions-v0.2`.

| Provider | Mode | Type | Primary route | Device evidence |
| --- | --- | --- | --- | --- |
| Google | Search | Text | Android web search / Google URL fallback | Existing search route |
| Google | Gemini | Text | `ACTION_PROCESS_TEXT` → Google app | Query text appeared verbatim in Gemini composer |
| YouTube | Search | Text | YouTube search URL targeted to app | Existing search route |
| YouTube | Shorts | Action | `com.google.android.youtube.action.open.shorts` | Present in installed YouTube launcher shortcuts; device launch reached YouTube |
| YouTube | Subscriptions | Action | `com.google.android.youtube.action.open.subscriptions` | Present in installed YouTube launcher shortcuts |
| Instagram | Search | Text | Instagram keyword-search URL | Existing search route |
| Instagram | Story | Action | `instagram://story-camera` | Resolves in installed Instagram |
| Instagram | Reel | Action | `instagram://reels-camera` | Resolves in installed Instagram; reached camera-permission flow |
| Instagram | Messages | Action | `instagram://direct-inbox` | Device launch reached Direct UI |
| TikTok | Search | Text | TikTok search URL | Existing search route |
| TikTok | Create | Action | `snssdk1233://aweme/create` | Resolves in installed TikTok; current account is logged out |
| TikTok | Inbox | Action | `snssdk1233://aweme/notification` | Resolves in installed TikTok; current account is logged out |
| ChatGPT | New chat | Text | targeted text-share → native/web fallback | Existing verified route |
| ChatGPT | Voice | Action | ChatGPT voice route | Existing verified route |
| ChatGPT | Camera | Action | system camera → image share to ChatGPT | Existing verified route |
| ChatGPT | Photo | Action | Android picker → image share to ChatGPT | Existing verified route |
| ChatGPT | Dictation | Action | Android speech recognizer → ChatGPT prompt | Existing verified route |

## Instagram direct-message limitation

Installed Instagram exposes a reliable Direct inbox shortcut, but not a stable external contract for converting an arbitrary username into a Direct thread.

The v0.2 contract therefore implements **Messages → Direct inbox**. It does not invent a private or unsupported thread URL.

## Gemini route selection

The following were tested on the connected device:

- `https://gemini.google.com/app?q=...` targeted to the Google app opened Gemini but did **not** populate the question.
- targeted `ACTION_SEND text/plain` went to Google Save and was unsuitable.
- targeted `ACTION_PROCESS_TEXT` resolved to Google's Gemini process-text gateway and populated the typed Widget Bar text exactly.

Therefore `ACTION_PROCESS_TEXT` is the authoritative Gemini submit route.
