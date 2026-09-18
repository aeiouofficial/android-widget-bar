from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

manifest = ROOT / "app/src/main/AndroidManifest.xml"
layout = ROOT / "app/src/main/res/layout/widget_search_bar.xml"
providers = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderTarget.java"
picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/PickerActivity.java"
search_activity = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchActivity.java"
search_launcher = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchLauncher.java"
widget_provider = ROOT / "app/src/main/java/com/aeiou/widgetbar/SearchBarWidgetProvider.java"
tap_policy = ROOT / "app/src/main/java/com/aeiou/widgetbar/TapGesturePolicy.java"
chat_actions = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptActionsActivity.java"
chat_media = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptMediaActivity.java"
chat = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptNewChatActivity.java"
setup = ROOT / "app/src/main/java/com/aeiou/widgetbar/SetupActivity.java"
pin_policy = ROOT / "app/src/main/java/com/aeiou/widgetbar/LauncherPinPolicy.java"

failures = []

for xml_path in ROOT.glob("app/src/main/**/*.xml"):
    try:
        ET.parse(xml_path)
    except ET.ParseError as exc:
        failures.append(f"XML parse failed: {xml_path.relative_to(ROOT)}: {exc}")

manifest_text = manifest.read_text(encoding="utf-8")
layout_text = layout.read_text(encoding="utf-8")
providers_text = providers.read_text(encoding="utf-8")
picker_text = picker.read_text(encoding="utf-8")
search_text = search_activity.read_text(encoding="utf-8")
launcher_text = search_launcher.read_text(encoding="utf-8")
widget_text = widget_provider.read_text(encoding="utf-8")
tap_policy_text = tap_policy.read_text(encoding="utf-8")
chat_actions_text = chat_actions.read_text(encoding="utf-8")
chat_media_text = chat_media.read_text(encoding="utf-8")
chat_text = chat.read_text(encoding="utf-8")
setup_text = setup.read_text(encoding="utf-8")
pin_policy_text = pin_policy.read_text(encoding="utf-8")

if "<uses-permission" in manifest_text:
    failures.append("Manifest must not request runtime/system permissions.")

for activity in (
        ".SearchActivity",
        ".ChatGptActionsActivity",
        ".ChatGptMediaActivity"):
    if activity not in manifest_text:
        failures.append(f"Manifest missing required activity: {activity}")

if ".TapRouterReceiver" in manifest_text:
    failures.append("TapRouterReceiver must not remain registered; double-tap is handled in SearchActivity.")

if "android.intent.action.MAIN" not in manifest_text or "android.intent.category.HOME" not in manifest_text:
    failures.append("Manifest must declare HOME intent visibility for default-launcher detection.")

if 'android:windowSoftInputMode="adjustResize"' not in manifest_text:
    failures.append("SearchActivity must use IME resize behavior.")

if "stateAlwaysVisible" in manifest_text:
    failures.append("IME must not auto-open before the double-tap window expires.")

for view_id in ("provider_icon", "search_hint", "selector_icon"):
    if layout_text.count(view_id) != 1:
        failures.append(f"Collapsed widget must contain exactly one {view_id}.")

if 'android:id="@+id/widget_pill"' not in layout_text:
    failures.append("Collapsed widget must use a dedicated compact pill container.")

if 'android:layout_height="48dp"' not in layout_text or 'android:layout_gravity="center_vertical"' not in layout_text:
    failures.append("Collapsed pill must stay compact and visually match edit mode.")

for provider in ("GOOGLE", "YOUTUBE", "INSTAGRAM", "TIKTOK", "CHATGPT"):
    if provider not in providers_text:
        failures.append(f"Missing selectable app target: {provider}")

if "setOrientation(LinearLayout.VERTICAL)" not in picker_text:
    failures.append("App selector must be a vertical drop-up.")

if "anchorTopY - selector.getHeight() - gap" not in picker_text:
    failures.append("App selector must sit fully above its anchor.")

if "EditText" in picker_text:
    failures.append("App selector must remain icon-only.")

if "new Intent(context, SearchActivity.class)" not in widget_text:
    failures.append("Writing bar must open SearchActivity.")

if "putExtra(SearchActivity.EXTRA_WIDGET_DOUBLE_TAP, true)" not in widget_text:
    failures.append("Writing bar must mark SearchActivity as double-tap aware.")

if "new Intent(context, ChatGptActionsActivity.class)" not in widget_text:
    failures.append("Selected ChatGPT icon must expose ChatGPT quick actions.")

if "new Intent(context, PickerActivity.class)" not in widget_text:
    failures.append("Right selector must still open PickerActivity.")

if "ChatGptNewChatActivity.class" in widget_text:
    failures.append("Collapsed widget must never launch ChatGPT directly.")

if "ViewConfiguration.getDoubleTapTimeout()" not in search_text:
    failures.append("SearchActivity must use Android's double-tap timeout.")

if "TapGesturePolicy.isDoubleTap" not in search_text:
    failures.append("SearchActivity must use the unit-tested double-tap timing policy.")

if "onNewIntent" not in search_text or "EXTRA_WIDGET_DOUBLE_TAP" not in search_text:
    failures.append("SearchActivity must capture the second widget PendingIntent for double-tap.")

if "FLAG_NOT_TOUCHABLE" not in search_text:
    failures.append("SearchActivity must pass very early second taps through to the launcher during the double-tap window.")

if "onWindowFocusChanged" not in search_text or "dispatchTouchEvent" not in search_text:
    failures.append("SearchActivity must switch to direct foreground capture once its window is ready, covering later taps in the same double-tap window.")

if "SearchLauncher.openAppHome(this, selected)" not in search_text:
    failures.append("Double-tap must open the selected app normally.")

if "handler.postDelayed(beginEditingRunnable, doubleTapTimeout)" not in search_text:
    failures.append("Single-tap editing must wait through the double-tap disambiguation window.")

if "searchField.requestFocus()" not in search_text or "showSoftInput" not in search_text:
    failures.append("Single tap must still activate the local editable surface and keyboard.")

if "secondTapAt - firstTapAt <= timeoutMillis" not in tap_policy_text:
    failures.append("Double-tap policy must enforce a bounded timing window.")

if "static boolean openAppHome" not in launcher_text:
    failures.append("SearchLauncher must expose the normal app-home route.")

if "getLaunchIntentForPackage" not in launcher_text:
    failures.append("Normal app-home route must prefer the installed app launcher intent.")

if "SOFT_INPUT_ADJUST_RESIZE" not in search_text or "getWindowVisibleDisplayFrame" not in search_text:
    failures.append("SearchActivity must remain keyboard-aware.")

if "if (SearchLauncher.launch(this, selected, searchField.getText().toString()))" not in search_text:
    failures.append("Provider handoff must still require explicit submit.")

if "SearchBarWidgetProvider.setEditing(this, true)" not in search_text:
    failures.append("SearchActivity must hide the launcher widget while editing.")

if "View.INVISIBLE" not in widget_text:
    failures.append("Launcher widget must be hidden during edit mode to avoid duplicates.")

if "setOrientation(LinearLayout.VERTICAL)" not in chat_actions_text:
    failures.append("ChatGPT quick actions must be a compact vertical pop-up.")

if "getIntent().getSourceBounds()" not in chat_actions_text:
    failures.append("ChatGPT quick-action menu must anchor above the tapped left icon when source bounds are available.")

for action in ("ACTION_VOICE", "ACTION_CAMERA", "ACTION_PHOTO", "ACTION_DICTATION"):
    if action not in chat_actions_text:
        failures.append(f"ChatGPT quick-action menu missing {action}.")

for icon in ("ic_voice", "ic_camera", "ic_image", "ic_mic"):
    if icon not in chat_actions_text:
        failures.append(f"ChatGPT quick-action menu missing {icon}.")

if "https://chatgpt.com/voice" not in chat_media_text:
    failures.append("Voice quick action must use ChatGPT's voice deep link.")

if "MediaStore.ACTION_IMAGE_CAPTURE" not in chat_media_text:
    failures.append("Camera quick action must use the system camera contract.")

if "MediaStore.ACTION_PICK_IMAGES" not in chat_media_text or "Intent.ACTION_OPEN_DOCUMENT" not in chat_media_text:
    failures.append("Photo quick action must use Android photo-picker/document fallbacks.")

if "RecognizerIntent.ACTION_RECOGNIZE_SPEECH" not in chat_media_text:
    failures.append("Dictation quick action must use the Android speech-recognition contract.")

if "Intent.ACTION_SEND" not in chat_media_text or "Intent.EXTRA_STREAM" not in chat_media_text:
    failures.append("Photo/camera result must be handed to ChatGPT through a standard image share.")

if "ChatGptNewChatActivity.EXTRA_PROMPT" not in chat_media_text:
    failures.append("Dictation result must be handed to ChatGPT as the prompt.")

guard_index = launcher_text.find("if (!hasSubmitText(query))")
chat_index = launcher_text.find("if (target.createAction)")
if guard_index < 0 or chat_index < 0 or guard_index > chat_index:
    failures.append("SearchLauncher must reject empty input before provider/create actions.")

if "Intent.ACTION_SEND" not in chat_text or "Intent.EXTRA_TEXT" not in chat_text:
    failures.append("ChatGPT text submit must retain its text-share handoff.")

if "chatgpt://" not in chat_text or "https://chatgpt.com/" not in chat_text:
    failures.append("ChatGPT text submit must retain native/browser fallbacks.")

if "requestPinAppWidget(provider, null, null)" not in setup_text:
    failures.append("Supported launchers must leave automatic pin completion to the launcher.")

if "PendingIntent" in setup_text:
    failures.append("SetupActivity must not reintroduce a pin success callback.")

if "LauncherPinPolicy.requiresManualPlacement" not in setup_text:
    failures.append("SetupActivity must retain launcher compatibility policy.")

if "com.android.launcher3" not in pin_policy_text:
    failures.append("AOSP/Lineage Launcher3 must retain manual widget placement.")

if failures:
    for failure in failures:
        print(f"FAIL: {failure}", file=sys.stderr)
    sys.exit(1)

print("PASS: XML parses and manifest remains permission-free")
print("PASS: compact 48dp idle/edit pill contract")
print("PASS: right selector remains vertical and icon-only")
print("PASS: single tap activates the local editor after double-tap disambiguation")
print("PASS: foreground second tap opens the selected app normally")
print("PASS: provider handoff still requires non-empty explicit submit")
print("PASS: edit surface remains keyboard-aware and duplicate-free")
print("PASS: ChatGPT left icon exposes voice/camera/photo/dictation actions")
print("PASS: ChatGPT media actions use Android system contracts without new permissions")
print("PASS: launcher placement safety remains intact")
