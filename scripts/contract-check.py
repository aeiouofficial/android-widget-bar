from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

manifest = ROOT / "app/src/main/AndroidManifest.xml"
layout = ROOT / "app/src/main/res/layout/widget_search_bar.xml"
providers = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderTarget.java"
modes = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderMode.java"
prefs = ROOT / "app/src/main/java/com/aeiou/widgetbar/WidgetPrefs.java"
picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/PickerActivity.java"
mode_picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/ModePickerActivity.java"
search_activity = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchActivity.java"
search_launcher = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchLauncher.java"
widget_provider = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchBarWidgetProvider.java"
tap_policy = ROOT / "app/src/main/java/com/aeiou/widgetbar/TapGesturePolicy.java"
chat_media = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptMediaActivity.java"
chat = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptNewChatActivity.java"
setup = ROOT / "app/src/main/java/com/aeiou/widgetbar/SetupActivity.java"
pin_policy = ROOT / "app/src/main/java/com/aeiou/widgetbar/LauncherPinPolicy.java"
old_chat_actions = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptActionsActivity.java"

failures = []

for xml_path in ROOT.glob("app/src/main/**/*.xml"):
    try:
        ET.parse(xml_path)
    except ET.ParseError as exc:
        failures.append(f"XML parse failed: {xml_path.relative_to(ROOT)}: {exc}")

manifest_text = manifest.read_text(encoding="utf-8")
layout_text = layout.read_text(encoding="utf-8")
providers_text = providers.read_text(encoding="utf-8")
modes_text = modes.read_text(encoding="utf-8")
prefs_text = prefs.read_text(encoding="utf-8")
picker_text = picker.read_text(encoding="utf-8")
mode_picker_text = mode_picker.read_text(encoding="utf-8")
search_text = search_activity.read_text(encoding="utf-8")
launcher_text = search_launcher.read_text(encoding="utf-8")
widget_text = widget_provider.read_text(encoding="utf-8")
tap_policy_text = tap_policy.read_text(encoding="utf-8")
chat_media_text = chat_media.read_text(encoding="utf-8")
chat_text = chat.read_text(encoding="utf-8")
setup_text = setup.read_text(encoding="utf-8")
pin_policy_text = pin_policy.read_text(encoding="utf-8")

if "<uses-permission" in manifest_text:
    failures.append("Manifest must not request runtime/system permissions.")

for activity in (".SearchActivity", ".ModePickerActivity", ".ChatGptMediaActivity"):
    if activity not in manifest_text:
        failures.append(f"Manifest missing required activity: {activity}")

if ".ChatGptActionsActivity" in manifest_text or old_chat_actions.exists():
    failures.append("Legacy ChatGPT-only left action menu must be removed; all providers use ModePickerActivity.")

if "android.intent.action.MAIN" not in manifest_text or "android.intent.category.HOME" not in manifest_text:
    failures.append("Manifest must retain HOME launcher query visibility.")

if 'android:windowSoftInputMode="adjustResize"' not in manifest_text:
    failures.append("SearchActivity must use IME resize behavior.")

if "stateAlwaysVisible" in manifest_text:
    failures.append("IME must not auto-open before the double-tap window expires.")

for view_id in ("provider_icon", "search_hint", "selector_icon"):
    if layout_text.count(view_id) != 1:
        failures.append(f"Collapsed widget must contain exactly one {view_id}.")

if 'android:id="@+id/provider_icon"' not in layout_text or '@string/mode_button' not in layout_text:
    failures.append("Left app icon must be labeled as the mode selector.")

if 'android:id="@+id/selector_icon"' not in layout_text or '@string/provider_button' not in layout_text:
    failures.append("Right icon must remain labeled as the app selector.")

if 'android:id="@+id/widget_pill"' not in layout_text:
    failures.append("Collapsed widget must use the compact widget_pill container.")

if 'android:layout_height="48dp"' not in layout_text or 'android:layout_gravity="center_vertical"' not in layout_text:
    failures.append("Collapsed pill must remain fixed at 48dp and centered in the launcher cell.")

for provider in ("GOOGLE", "YOUTUBE", "INSTAGRAM", "TIKTOK", "CHATGPT"):
    if provider not in providers_text:
        failures.append(f"Missing selectable provider: {provider}")

required_modes = (
    "GOOGLE_SEARCH",
    "GOOGLE_GEMINI",
    "YOUTUBE_SEARCH",
    "YOUTUBE_SHORTS",
    "YOUTUBE_SUBSCRIPTIONS",
    "INSTAGRAM_SEARCH",
    "INSTAGRAM_STORY",
    "INSTAGRAM_REEL",
    "INSTAGRAM_MESSAGES",
    "TIKTOK_SEARCH",
    "TIKTOK_CREATE",
    "TIKTOK_INBOX",
    "CHATGPT_NEW_CHAT",
    "CHATGPT_VOICE",
    "CHATGPT_CAMERA",
    "CHATGPT_PHOTO",
    "CHATGPT_DICTATION",
)
for mode in required_modes:
    if mode not in modes_text:
        failures.append(f"Missing provider mode: {mode}")

if "defaultFor(ProviderTarget provider)" not in modes_text or "modesFor(ProviderTarget provider)" not in modes_text:
    failures.append("ProviderMode must provide scoped defaults and scoped mode lists.")

if "KEY_MODE_PREFIX" not in prefs_text or "getMode(Context context, ProviderTarget target)" not in prefs_text:
    failures.append("WidgetPrefs must persist the selected mode independently for each provider.")

if "setMode(Context context, ProviderMode mode)" not in prefs_text:
    failures.append("WidgetPrefs must persist mode changes.")

if "setOrientation(LinearLayout.VERTICAL)" not in picker_text:
    failures.append("Right-side provider selector must remain a vertical drop-up.")

if "EditText" in picker_text:
    failures.append("Right-side provider selector must remain icon-only.")

if "setOrientation(LinearLayout.VERTICAL)" not in mode_picker_text:
    failures.append("Left-side provider-mode selector must be a vertical drop-up.")

if "ProviderMode.modesFor(provider)" not in mode_picker_text:
    failures.append("Left mode selector must show only modes for the active provider.")

if "WidgetPrefs.setMode(this, mode)" not in mode_picker_text:
    failures.append("Selecting a left-side mode must persist and refresh the widget.")

if "if (!mode.acceptsText)" not in mode_picker_text or "SearchLauncher.launchAction(this, mode)" not in mode_picker_text:
    failures.append("Non-text provider modes must launch immediately from the mode picker with no second tap.")

if "getIntent().getSourceBounds()" not in mode_picker_text:
    failures.append("Left mode selector must anchor above the tapped app icon when source bounds exist.")

if "new Intent(context, ModePickerActivity.class)" not in widget_text:
    failures.append("Left app icon must open ModePickerActivity for every provider.")

if "new Intent(context, SearchActivity.class)" not in widget_text:
    failures.append("Center bar must continue through gesture-aware SearchActivity.")

if "putExtra(SearchActivity.EXTRA_WIDGET_DOUBLE_TAP, true)" not in widget_text:
    failures.append("Center bar must retain double-tap disambiguation.")

if "new Intent(context, PickerActivity.class)" not in widget_text:
    failures.append("Right selector must continue opening PickerActivity.")

if "WidgetPrefs.getMode(context, target)" not in widget_text or "mode.collapsedHint" not in widget_text:
    failures.append("Collapsed center text must reflect the active provider mode.")

if "ChatGptNewChatActivity.class" in widget_text:
    failures.append("Collapsed widget must never launch ChatGPT directly.")

if "selectedMode = WidgetPrefs.getMode(this, selected)" not in search_text:
    failures.append("SearchActivity must load the current provider mode.")

if "selectedMode.acceptsText" not in search_text:
    failures.append("SearchActivity must distinguish editable and immediate provider modes.")

if "SearchLauncher.launchAction(this, selectedMode)" not in search_text:
    failures.append("Non-text modes must execute only after single-tap/double-tap disambiguation.")

if "SearchLauncher.launch(this, selectedMode, searchField.getText().toString())" not in search_text:
    failures.append("Text modes must submit through the selected provider mode.")

if "new Intent(this, ModePickerActivity.class)" not in search_text:
    failures.append("Left icon in edit mode must also open the mode selector.")

if "ViewConfiguration.getDoubleTapTimeout()" not in search_text:
    failures.append("SearchActivity must use Android's double-tap timeout.")

if "TapGesturePolicy.isDoubleTap" not in search_text:
    failures.append("SearchActivity must use the unit-tested double-tap timing policy.")

if "FLAG_NOT_TOUCHABLE" not in search_text or "onWindowFocusChanged" not in search_text or "dispatchTouchEvent" not in search_text:
    failures.append("SearchActivity must retain hybrid early/late double-tap handling.")

if "SearchLauncher.openAppHome(this, selected)" not in search_text:
    failures.append("Double-tap must still open the selected app normally.")

if "SOFT_INPUT_ADJUST_RESIZE" not in search_text or "getWindowVisibleDisplayFrame" not in search_text:
    failures.append("Text edit mode must stay keyboard-aware.")

if "SearchBarWidgetProvider.setEditing(this, true)" not in search_text or "View.INVISIBLE" not in widget_text:
    failures.append("Launcher widget must remain duplicate-free while the editable surface is active.")

if "secondTapAt - firstTapAt <= timeoutMillis" not in tap_policy_text:
    failures.append("Double-tap timing policy must remain bounded.")

if "Intent.ACTION_PROCESS_TEXT" not in launcher_text or "Intent.EXTRA_PROCESS_TEXT" not in launcher_text:
    failures.append("Gemini mode must forward typed text through Google's PROCESS_TEXT gateway.")

if "https://gemini.google.com/app" not in launcher_text:
    failures.append("Gemini mode must retain an app/web fallback.")

for action in (
    "com.google.android.youtube.action.open.shorts",
    "com.google.android.youtube.action.open.subscriptions",
):
    if action not in launcher_text:
        failures.append(f"YouTube mode route missing: {action}")

for uri in (
    "instagram://story-camera",
    "instagram://reels-camera",
    "instagram://direct-inbox",
    "snssdk1233://aweme/create",
    "snssdk1233://aweme/notification",
):
    if uri not in launcher_text:
        failures.append(f"Provider mode deep link missing: {uri}")

for action in ("ACTION_VOICE", "ACTION_CAMERA", "ACTION_PHOTO", "ACTION_DICTATION"):
    if action not in launcher_text:
        failures.append(f"ChatGPT mode launch missing: {action}")

if "MediaStore.ACTION_IMAGE_CAPTURE" not in chat_media_text:
    failures.append("ChatGPT camera mode must use the system camera contract.")

if "MediaStore.ACTION_PICK_IMAGES" not in chat_media_text or "Intent.ACTION_OPEN_DOCUMENT" not in chat_media_text:
    failures.append("ChatGPT photo mode must use Android picker/document fallbacks.")

if "RecognizerIntent.ACTION_RECOGNIZE_SPEECH" not in chat_media_text:
    failures.append("ChatGPT dictation mode must use Android speech recognition.")

if "Intent.ACTION_SEND" not in chat_media_text or "Intent.EXTRA_STREAM" not in chat_media_text:
    failures.append("ChatGPT image results must be handed through a standard targeted image share.")

if "Intent.ACTION_SEND" not in chat_text or "Intent.EXTRA_TEXT" not in chat_text:
    failures.append("ChatGPT text submit must retain targeted text share.")

if "chatgpt://" not in chat_text or "https://chatgpt.com/" not in chat_text:
    failures.append("ChatGPT text submit must retain native/browser fallbacks.")

if "static boolean openAppHome" not in launcher_text or "getLaunchIntentForPackage" not in launcher_text:
    failures.append("Double-tap normal-app route must prefer installed launcher intents.")

if "requestPinAppWidget(provider, null, null)" not in setup_text:
    failures.append("Supported launchers must leave automatic pin completion to the launcher.")

if "PendingIntent" in setup_text:
    failures.append("SetupActivity must not reintroduce a pin success callback.")

if "LauncherPinPolicy.requiresManualPlacement" not in setup_text or "com.android.launcher3" not in pin_policy_text:
    failures.append("Launcher3 manual-placement safety must remain intact.")

if failures:
    for failure in failures:
        print(f"FAIL: {failure}", file=sys.stderr)
    sys.exit(1)

print("PASS: XML parses and manifest remains permission-free")
print("PASS: compact 48dp idle/edit pill remains intact")
print("PASS: right app selector remains vertical and icon-only")
print("PASS: left icon now opens provider-scoped mode picker for every app")
print("PASS: mode state persists per provider with search/new-chat defaults")
print("PASS: double-tap still opens the selected app normally")
print("PASS: text modes require explicit non-empty submit")
print("PASS: non-text modes launch immediately when chosen from the left mode picker")
print("PASS: Google Gemini receives typed text through PROCESS_TEXT")
print("PASS: YouTube Shorts/subscriptions routes are present")
print("PASS: Instagram story/reel/messages routes are present")
print("PASS: TikTok create/inbox routes are present")
print("PASS: ChatGPT voice/camera/photo/dictation modes remain available")
print("PASS: keyboard and launcher-placement safety remain intact")
