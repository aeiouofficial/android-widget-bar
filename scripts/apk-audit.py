from pathlib import Path
import os
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
APK = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
SDK = Path(os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME") or "")
BUILD_TOOLS = SDK / "build-tools" / "35.0.0"

def fail(message: str) -> None:
    print(f"FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)

def find_tool(name: str) -> Path:
    candidates = [BUILD_TOOLS / name]
    if os.name == "nt":
        candidates += [BUILD_TOOLS / f"{name}.exe", BUILD_TOOLS / f"{name}.bat"]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    fail(f"Android build tool not found: {name} under {BUILD_TOOLS}")

def run(tool: Path, *args: str) -> str:
    command = [str(tool), *args]
    if os.name == "nt" and tool.suffix.lower() == ".bat":
        command = ["cmd", "/d", "/c", str(tool), *args]
    result = subprocess.run(
        command,
        cwd=ROOT,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    output = (result.stdout or "") + (result.stderr or "")
    if result.returncode != 0:
        print(output, file=sys.stderr)
        fail(f"{tool.name} exited with {result.returncode}")
    return output

if not APK.is_file():
    fail(f"APK missing: {APK.relative_to(ROOT)}")
if not SDK.is_dir():
    fail("ANDROID_SDK_ROOT or ANDROID_HOME must point to an Android SDK")

aapt = find_tool("aapt")
zipalign = find_tool("zipalign")
apksigner = find_tool("apksigner")

badging = run(aapt, "dump", "badging", str(APK))
required_badging = (
    "package: name='com.aeiou.widgetbar'",
    "sdkVersion:'26'",
    "targetSdkVersion:'36'",
    "provides-component:'app-widget'",
)
for marker in required_badging:
    if marker not in badging:
        fail(f"APK badging missing: {marker}")

permissions = run(aapt, "dump", "permissions", str(APK))
if re.search(r"(?m)^uses-permission", permissions):
    fail("APK unexpectedly declares a permission")

run(zipalign, "-c", "4", str(APK))
signature = run(apksigner, "verify", "--verbose", str(APK))
if "Verifies" not in signature:
    fail("APK signature verification did not report success")

print("PASS: APK identity com.aeiou.widgetbar")
print("PASS: minSdk 26 / targetSdk 36")
print("PASS: app-widget component present")
print("PASS: zero declared permissions")
print("PASS: ZIP alignment verified")
print("PASS: APK signature verified")
