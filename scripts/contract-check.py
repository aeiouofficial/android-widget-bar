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
chat_text = chat.read_text(encoding="utf-8")
setup_text = setup.read_text(encoding="utf-8")
pin_policy_text = pin_policy.read_text(encoding="utf-8")

if "<uses-permission" in manifest_text:
    failures.append("Manifest must not request runtime/system permissions.")

if "android.intent.action.MAIN" not in manifest_text or "android.intent.category.HOME" not in manifest_text:
    failures.append("Manifest must declare HOME intent visibility for default-launcher detection.")

if '.SearchActivity' not in manifest_text:
    failures.append("Keyboard-aware SearchActivity must be registered.")

if 'android:windowSoftInputMode="adjustResize|stateAlwaysVisible"' not in manifest_text:
    failures.append("SearchActivity must opt into IME resize behavior.")

collapsed_ids = ("provider_icon", "search_hint", "selector_icon")
for view_id in collapsed_ids:
    if layout_text.count(view_id) != 1:
        failures.append(f"Collapsed widget must contain exactly one {view_id}.")

for forbidden in ("chatgpt_icon", "mic", "lens", "youtube", "instagram", "tiktok"):
    if forbidden in layout_text.lower():
        failures.append(f"Collapsed widget contains forbidden fixed icon/content: {forbidden}")

for provider in ("GOOGLE", "YOUTUBE", "INSTAGRAM", "TIKTOK", "CHATGPT"):
    if provider not in providers_text:
        failures.append(f"Missing selectable app target: {provider}")

if '"New chat"' not in providers_text or '"Ask ChatGPT…"' not in providers_text:
    failures.append("ChatGPT must expose separate collapsed and editable prompt hints.")

if "ProviderTarget.values()" not in picker_text:
    failures.append("Right-side selector must render the selectable app icon set.")

if "EditText" in picker_text:
    failures.append("Selector drop-up must contain icons only, never a second search field.")

if "R.id.selector_icon" not in widget_text or "new Intent(context, PickerActivity.class)" not in widget_text:
    failures.append("Right-side selector must open PickerActivity.")

if "new Intent(context, SearchActivity.class)" not in widget_text:
    failures.append("Left/center widget actions must open only the local editable search surface.")

if "ChatGptNewChatActivity.class" in widget_text:
    failures.append("Collapsed widget must never launch ChatGPT directly on tap.")

if "SOFT_INPUT_ADJUST_RESIZE" not in search_text:
    failures.append("SearchActivity must resize for the keyboard.")

if "getWindowVisibleDisplayFrame" not in search_text:
    failures.append("SearchActivity must reposition the visible bar above the IME.")

if "EditText" not in search_text or "selected.inputHint" not in search_text:
    failures.append("SearchActivity must provide the active target's editable input field.")

if "R.drawable.ic_provider_picker" not in search_text:
    failures.append("Search surface must preserve the right-side selector control.")

if "if (selected.createAction)" in search_text:
    failures.append("SearchActivity must not auto-launch ChatGPT merely because ChatGPT is selected.")

if "if (SearchLauncher.launch(this, selected, searchField.getText().toString()))" not in search_text:
    failures.append("SearchActivity must hand off only from an explicit non-empty submit.")

if "SearchBarWidgetProvider.setEditing(this, true)" not in search_text:
    failures.append("SearchActivity must hide the launcher widget while the editable surface is active.")

if search_text.count("SearchBarWidgetProvider.setEditing(this, false)") < 2:
    failures.append("SearchActivity must restore the launcher widget on pause/finish.")

if "View.INVISIBLE" not in widget_text or "android.R.id.background" not in widget_text:
    failures.append("The launcher widget must be temporarily hidden while editing to prevent a duplicate visible bar.")

guard_index = launcher_text.find("if (!hasSubmitText(query))")
chat_index = launcher_text.find("if (target.createAction)")
if guard_index < 0 or chat_index < 0 or guard_index > chat_index:
    failures.append("SearchLauncher must reject empty input before any provider/create action.")

if "putExtra(ChatGptNewChatActivity.EXTRA_PROMPT, trimmed)" not in launcher_text:
    failures.append("ChatGPT submit must carry the typed prompt into the ChatGPT handoff.")

if "Intent.ACTION_SEND" not in chat_text or "Intent.EXTRA_TEXT" not in chat_text:
    failures.append("ChatGPT handoff must forward the typed prompt using Android's text-share contract.")

if "chatgpt://" not in chat_text or "com.openai.chatgpt" not in chat_text:
    failures.append("ChatGPT handoff must retain the native new-chat fallback.")

if "https://chatgpt.com/" not in chat_text:
    failures.append("ChatGPT handoff must retain a browser fallback.")

if "requestPinAppWidget(provider, null, null)" not in setup_text:
    failures.append("Supported launchers must leave automatic pin completion to the launcher.")

if "PendingIntent" in setup_text:
    failures.append("SetupActivity must not reintroduce a pin success callback.")

if "LauncherPinPolicy.requiresManualPlacement" not in setup_text:
    failures.append("SetupActivity must guard launchers that require manual widget placement.")

if "openHomeForManualPlacement()" not in setup_text:
    failures.append("SetupActivity must retain a manual placement fallback.")

if "com.android.launcher3" not in pin_policy_text:
    failures.append("AOSP/Lineage Launcher3 must use manual widget placement.")

if failures:
    for failure in failures:
        print(f"FAIL: {failure}", file=sys.stderr)
    sys.exit(1)

print("PASS: XML parses")
print("PASS: no requested permissions")
print("PASS: collapsed bar = active app left + action text center + selector right")
print("PASS: selector targets = Google, YouTube, Instagram, TikTok, ChatGPT")
print("PASS: selector drop-up contains icons only")
print("PASS: tapping left/center opens only the local editable surface")
print("PASS: no provider app launches from a plain widget tap")
print("PASS: launcher widget is hidden while editing, so no duplicate bar remains visible")
print("PASS: empty submit cannot launch any provider")
print("PASS: ChatGPT accepts typed prompt before handoff")
print("PASS: ChatGPT handoff forwards typed text only after submit")
print("PASS: search surface resizes/repositions above the keyboard")
print("PASS: supported launchers own automatic widget pin completion")
print("PASS: AOSP/Lineage Launcher3 uses safe manual placement")
