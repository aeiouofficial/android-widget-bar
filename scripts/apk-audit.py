from pathlib import Path
import os
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
APK = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
SDK = Path(os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME") or "")
BUILD_TOOLS_ROOT = SDK / "build-tools"


def fail(message: str) -> None:
    print(f"FAIL: {message}", file=sys.stderr)
    raise SystemExit(1)


def version_key(path: Path) -> tuple[int, ...]:
    parts = re.findall(r"\d+", path.name)
    return tuple(int(part) for part in parts)


def select_build_tools() -> Path:
    requested = os.environ.get("ANDROID_BUILD_TOOLS_VERSION")
    if requested:
        selected = BUILD_TOOLS_ROOT / requested
        if not selected.is_dir():
            fail(f"Requested Android build-tools missing: {selected}")
        return selected

    if not BUILD_TOOLS_ROOT.is_dir():
        fail(f"Android build-tools directory missing: {BUILD_TOOLS_ROOT}")

    candidates = sorted(
        (path for path in BUILD_TOOLS_ROOT.iterdir() if path.is_dir()),
        key=version_key,
        reverse=True,
    )
    for candidate in candidates:
        if all(
            any(
                (candidate / name).is_file()
                for name in (
                    tool,
                    f"{tool}.exe",
                    f"{tool}.bat",
                )
            )
            for tool in ("aapt", "zipalign", "apksigner")
        ):
            return candidate

    fail(f"No complete Android build-tools installation found under {BUILD_TOOLS_ROOT}")


def find_tool(build_tools: Path, name: str) -> Path:
    candidates = [build_tools / name]
    if os.name == "nt":
        candidates += [build_tools / f"{name}.exe", build_tools / f"{name}.bat"]
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    fail(f"Android build tool not found: {name} under {build_tools}")


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

build_tools = select_build_tools()
aapt = find_tool(build_tools, "aapt")
zipalign = find_tool(build_tools, "zipalign")
apksigner = find_tool(build_tools, "apksigner")

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

print(f"PASS: Android build-tools {build_tools.name}")
print("PASS: APK identity com.aeiou.widgetbar")
print("PASS: minSdk 26 / targetSdk 36")
print("PASS: app-widget component present")
print("PASS: zero declared permissions")
print("PASS: ZIP alignment verified")
print("PASS: APK signature verified")
