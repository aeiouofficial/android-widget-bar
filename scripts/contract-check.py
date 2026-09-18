from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
manifest = ROOT / "app/src/main/AndroidManifest.xml"
layout = ROOT / "app/src/main/res/layout/widget_search_bar.xml"
providers = ROOT / "app/src/main/java/com/aeiou/widgetbar/ProviderTarget.java"
picker = ROOT / "app/src/main/java/com/aeiou/widgetbar/PickerActivity.java"
chat = ROOT / "app/src/main/java/com/aeiou/widgetbar/ChatGptNewChatActivity.java"

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
chat_text = chat.read_text(encoding="utf-8")

if "<uses-permission" in manifest_text:
    failures.append("Manifest must not request runtime/system permissions.")

if layout_text.count("chatgpt_icon") != 1:
    failures.append("Collapsed widget must contain exactly one ChatGPT icon.")

for forbidden in ("mic", "lens", "youtube", "instagram", "tiktok"):
    if forbidden in layout_text.lower():
        failures.append(f"Collapsed widget contains forbidden extra icon/content: {forbidden}")

required = ("GOOGLE", "YOUTUBE", "INSTAGRAM", "TIKTOK")
for provider in required:
    if provider not in providers_text:
        failures.append(f"Missing search provider: {provider}")

if "CHATGPT" in providers_text:
    failures.append("ChatGPT must remain a separate new-chat action, not a search provider.")

if "ProviderTarget.values()" not in picker_text:
    failures.append("Drop-up must render the provider icon set.")

if "chatgpt://" not in chat_text or "com.openai.chatgpt" not in chat_text:
    failures.append("ChatGPT new-chat action must target the native ChatGPT app route.")

if "https://chatgpt.com/" not in chat_text:
    failures.append("ChatGPT new-chat action must retain the browser fallback.")

if failures:
    for failure in failures:
        print(f"FAIL: {failure}", file=sys.stderr)
    sys.exit(1)

print("PASS: XML parses")
print("PASS: no requested permissions")
print("PASS: exactly one right-side ChatGPT icon")
print("PASS: collapsed bar has no extra provider icons")
print("PASS: drop-up providers = Google, YouTube, Instagram, TikTok")
print("PASS: ChatGPT remains separate new-chat action")
