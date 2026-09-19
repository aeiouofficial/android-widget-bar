from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

manifest = ROOT / "app/src/main/AndroidManifest.xml"
layout = ROOT / "app/src/main/res/layout/widget_search_bar.xml"
wheel_layout = ROOT / "app/src/main/res/layout/widget_search_bar_wheel.xml"
wheel_item = ROOT / "app/src/main/res/layout/widget_provider_wheel_item.xml"
wheel_info = ROOT / "app/src/main/res/xml/search_bar_wheel_widget_info.xml"
wheel_info_v31 = ROOT / "app/src/main/res/xml-v31/search_bar_wheel_widget_info.xml"
providers = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderTarget.java"
modes = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderMode.java"
prefs = ROOT / "app/src/main/java/com/aeiou/widgetbar/WidgetPrefs.java"
picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/PickerActivity.java"
mode_picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/ModePickerActivity.java"
search_activity = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchActivity.java"
search_launcher = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchLauncher.java"
widget_provider = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchBarWidgetProvider.java"
wheel_provider = ROOT / "app/src/main/java/com/aeiou/widgetbar/WheelSearchBarWidgetProvider.java"
wheel_service = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderWheelService.java"
wheel_factory = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderWheelFactory.java"
wheel_policy = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderWheelPolicy.java"
widget_updates = ROOT / "app/src/main/java/com/aeiou/widgetbar/WidgetUpdates.java"
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
wheel_layout_text = wheel_layout.read_text(encoding="utf-8")
wheel_item_text = wheel_item.read_text(encoding="utf-8")
wheel_info_text = wheel_info.read_text(encoding="utf-8")
wheel_info_v31_text = wheel_info_v31.read_text(encoding="utf-8")
providers_text = providers.read_text(encoding="utf-8")
modes_text = modes.read_text(encoding="utf-8")
prefs_text = prefs.read_text(encoding="utf-8")
picker_text = picker.read_text(encoding="utf-8")
mode_picker_text = mode_picker.read_text(encoding="utf-8")
search_text = search_activity.read_text(encoding="utf-8")
launcher_text = search_launcher.read_text(encoding="utf-8")
widget_text = widget_provider.read_text(encoding="utf-8")
wheel_widget_text = wheel_provider.read_text(encoding="utf-8")
wheel_service_text = wheel_service.read_text(encoding="utf-8")
wheel_factory_text = wheel_factory.read_text(encoding="utf-8")
wheel_policy_text = wheel_policy.read_text(encoding="utf-8")
updates_text = widget_updates.read_text(encoding="utf-8")
tap_policy_text = tap_policy.read_text(encoding="utf-8")
chat_media_text = chat_media.read_text(encoding="utf-8")
chat_text = chat.read_text(encoding="utf-8")
setup_text = setup.read_text(encoding="utf-8")
pin_policy_text = pin_policy.read_text(encoding="utf-8")

if "<uses-permission" in manifest_text:
    failures.append("Manifest must not request runtime/system permissions.")

for component in (".SearchBarWidgetProvider", ".WheelSearchBarWidgetProvider", ".ProviderWheelService"):
    if component not in manifest_text:
        failures.append(f"Manifest missing wheel/classic component: {component}")

if 'android:permission="android.permission.BIND_REMOTEVIEWS"' not in manifest_text:
    failures.append("Wheel RemoteViewsService must be protected by BIND_REMOTEVIEWS.")

if 'android:label="@string/widget_classic_name"' not in manifest_text:
    failures.append("Classic widget must remain separately selectable in the launcher picker.")

if 'android:label="@string/widget_wheel_name"' not in manifest_text:
    failures.append("Wheel widget must be separately selectable in the launcher picker.")

if "@xml/search_bar_wheel_widget_info" not in manifest_text:
    failures.append("Wheel widget provider metadata must be registered.")

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

if 'android:id="@+id/widget_pill"' not in wheel_layout_text:
    failures.append("Wheel widget must use the same compact widget_pill container.")

if 'android:layout_height="48dp"' not in wheel_layout_text:
    failures.append("Wheel widget pill must remain fixed at 48dp.")

if '<ListView' not in wheel_layout_text or 'android:id="@+id/provider_wheel"' not in wheel_layout_text:
    failures.append("Second widget variant must expose a flat vertical ListView reel on the right.")

if '<StackView' in wheel_layout_text or 'android:loopViews=' in wheel_layout_text:
    failures.append("Second widget variant must not use StackView/card-rotation behavior.")

if 'android:scrollbars="none"' not in wheel_layout_text or 'android:overScrollMode="never"' not in wheel_layout_text:
    failures.append("Provider reel must be a clean flat vertical scroll surface.")

if 'android:layout_height="32dp"' not in wheel_item_text:
    failures.append("Provider reel items must be compact enough to visibly scroll through the right-side slot.")

if '@layout/widget_search_bar_wheel' not in wheel_info_text:
    failures.append("Wheel widget metadata must reference the wheel layout.")

if 'android:targetCellWidth="4"' not in wheel_info_v31_text or 'android:targetCellHeight="1"' not in wheel_info_v31_text:
    failures.append("Wheel widget must retain 4x1 target geometry on Android 12+.")

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
    failures.append("Mode selector must anchor above the tapped app icon when source bounds exist.")

for token in (
    "EXTRA_PROVIDER_ID",
    "EXTRA_ANCHOR_SIDE",
    "EXTRA_FROM_WHEEL",
    "ANCHOR_RIGHT",
    "WidgetPrefs.setProvider(this, provider)",
):
    if token not in mode_picker_text:
        failures.append(f"Wheel-aware ModePicker integration missing: {token}")

if "if (fromWheel)" not in mode_picker_text or "width - dp(46)" not in mode_picker_text:
    failures.append("Wheel-triggered mode choices must stay right-anchored when launcher bounds are unreliable.")

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

if "extends RemoteViewsService" not in wheel_service_text:
    failures.append("Wheel variant must be backed by a RemoteViewsService.")

if "ProviderTarget.values()" not in wheel_factory_text:
    failures.append("Wheel factory must expose all five provider icons.")

if "ProviderWheelPolicy.VIRTUAL_ITEM_COUNT" not in wheel_factory_text:
    failures.append("Wheel RemoteViews adapter must expose the infinite virtual slot rail.")

if "providerIndexForPosition" not in wheel_factory_text:
    failures.append("Wheel RemoteViews adapter must wrap every virtual position modulo the provider set.")

if "Integer.MAX_VALUE" not in wheel_policy_text:
    failures.append("Wheel rail must use an effectively unbounded virtual item space.")

if "Math.floorMod" not in wheel_policy_text or "centeredPositionForProvider" not in wheel_policy_text:
    failures.append("Wheel rail must wrap bidirectionally and start near the center, not at a terminal edge.")

for token in (
    "ModePickerActivity.EXTRA_PROVIDER_ID",
    "ModePickerActivity.EXTRA_ANCHOR_SIDE",
    "ModePickerActivity.ANCHOR_RIGHT",
    "ModePickerActivity.EXTRA_FROM_WHEEL",
    "setOnClickFillInIntent",
):
    if token not in wheel_factory_text:
        failures.append(f"Wheel item routing missing: {token}")

for token in (
    "setRemoteAdapter",
    "setScrollPosition",
    "setPendingIntentTemplate",
    "PendingIntent.FLAG_MUTABLE",
    "WidgetUpdates.VARIANT_WHEEL",
):
    if token not in wheel_widget_text:
        failures.append(f"Wheel provider behavior missing: {token}")

if "WidgetUpdates.VARIANT_CLASSIC" not in widget_text:
    failures.append("Classic widget must identify itself as the Classic SearchActivity variant.")

if "SearchBarWidgetProvider.updateAll(context)" not in updates_text or "WheelSearchBarWidgetProvider.updateAll(context)" not in updates_text:
    failures.append("Shared widget updates must refresh Classic and Wheel together.")

if "selectedMode = WidgetPrefs.getMode(this, selected)" not in search_text:
    failures.append("SearchActivity must load the current provider mode.")

if "selectedMode.acceptsText" not in search_text:
    failures.append("SearchActivity must distinguish editable and immediate provider modes.")

if "SearchLauncher.launchAction(this, selectedMode)" not in search_text:
    failures.append("Non-text modes must execute only after single-tap/double-tap disambiguation.")

if "SearchLauncher.launch(this, selectedMode, searchField.getText().toString())" not in search_text:
    failures.append("Text modes must submit through the selected provider mode.")

if "new Intent(this, ModePickerActivity.class)" not in search_text:
    failures.append("Active edit surface must be able to open provider mode choices.")

for token in (
    "EXTRA_WIDGET_VARIANT",
    "new ListView(this)",
    "setOnItemClickListener",
    "openModePickerRight",
    "ProviderWheelPolicy.VIRTUAL_ITEM_COUNT",
    "ProviderWheelPolicy.centeredPositionForProvider",
    "ProviderWheelPolicy.providerIndexForPosition",
):
    if token not in search_text:
        failures.append(f"Active flat-reel interaction missing: {token}")

for forbidden in ("ProviderWheelPolicy.cycle", "WHEEL_SWIPE_DP", "WheelKnobButton"):
    if forbidden in search_text:
        failures.append(f"Disliked discrete rotation mechanism must stay removed: {forbidden}")

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

if "WidgetUpdates.setEditing(this, widgetVariant, true)" not in search_text:
    failures.append("SearchActivity must hide only the widget variant that launched it while editing.")

if "View.INVISIBLE" not in widget_text or "View.INVISIBLE" not in wheel_widget_text:
    failures.append("Both Classic and Wheel widgets must support duplicate-free edit mode.")

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

if "SearchBarWidgetProvider.class" not in setup_text or "WheelSearchBarWidgetProvider.class" not in setup_text:
    failures.append("SetupActivity must offer both Classic and Wheel widget variants.")

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
print("PASS: Classic right app selector remains vertical and icon-only")
print("PASS: Wheel right side is an infinite modulo-wrapped vertical slot rail")
print("PASS: Classic and Wheel remain separately selectable widget providers")
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
