#!/bin/sh
set -e

PROJECT_DIR="/data/data/com.termux/files/home/fpsmoon"
OUTPUT_DIR="/sdcard/Download"

cd "$PROJECT_DIR"

if [ ! -f "module.prop" ]; then
    echo "error: module.prop not found"
    exit 1
fi

VERSION=$(grep '^version=' module.prop | cut -d= -f2)
VERSION_CODE=$(grep '^versionCode=' module.prop | cut -d= -f2)
ZIP_OUT="FPSMoon-${VERSION}.zip"

echo "=== Building FPS Moon ${VERSION} (${VERSION_CODE}) ==="

# 1. Compile C Telemetry Daemon
echo "[1/4] Compiling native C daemon..."
mkdir -p bin
clang -O3 -Wall -Wextra src/fpsmoon_daemon.c -o bin/fpsmoon_daemon
chmod 755 bin/fpsmoon_daemon

# 2. Compile Java Overlay if tools available
echo "[2/4] Checking Java overlay compiler..."
ANDROID_JAR="/data/data/com.termux/files/home/android-sdk/android.jar"
if [ ! -f "$ANDROID_JAR" ]; then
    ANDROID_JAR=$(find /data/data/com.termux/files/home -name "android.jar" 2>/dev/null | head -n 1)
fi

if [ -n "$ANDROID_JAR" ] && command -v ecj >/dev/null 2>&1 && (command -v dx >/dev/null 2>&1 || command -v d8 >/dev/null 2>&1); then
    echo "      Compiling FPSMoonOverlay.java with ecj & dx..."
    rm -rf /tmp/fpsmoon_build
    mkdir -p /tmp/fpsmoon_build/classes
    ecj -7 -cp "$ANDROID_JAR" -d /tmp/fpsmoon_build/classes src/FPSMoonOverlay.java
    if command -v dx >/dev/null 2>&1; then
        dx --dex --output=bin/fpsmoon.dex /tmp/fpsmoon_build/classes
    elif command -v d8 >/dev/null 2>&1; then
        d8 --output bin/ /tmp/fpsmoon_build/classes/com/fpsmoon/FPSMoonOverlay*.class
        mv bin/classes.dex bin/fpsmoon.dex
    fi
    rm -rf /tmp/fpsmoon_build
    echo "      Compiled bin/fpsmoon.dex successfully."
else
    echo "      Using existing prebuilt bin/fpsmoon.dex."
fi
chmod 644 bin/fpsmoon.dex

# 3. Build Vue 3 WebUI
if [ -d "webui" ]; then
    echo "[3/4] Compiling Vue 3 WebUI with Vite..."
    if [ ! -d "webui/node_modules" ]; then
        if [ -d "/data/data/com.termux/files/home/HyperCore_Module/webui/node_modules" ]; then
            ln -s /data/data/com.termux/files/home/HyperCore_Module/webui/node_modules webui/node_modules
        else
            echo "      Installing webui dependencies..."
            (cd webui && npm install --no-audit --no-fund)
        fi
    fi
    
    (cd webui && node ./node_modules/vite/bin/vite.js build)
    
    if [ ! -f "webui/dist/index.html" ]; then
        echo "error: webui output not found"
        exit 1
    fi
    
    mkdir -p webroot
    cp webui/dist/index.html webroot/index.html
    echo "      WebUI compiled & deployed to webroot/index.html."
fi

# 4. Packaging Magisk / KernelSU / APatch Module Zip
echo "[4/4] Packaging ${ZIP_OUT}..."
rm -f "${ZIP_OUT}"
zip -r9 "${ZIP_OUT}" \
    META-INF \
    action.sh \
    bin \
    customize.sh \
    module.prop \
    service.sh \
    state/config.json \
    state/position.json \
    uninstall.sh \
    webroot \
    README.md \
    LICENSE \
    -x "*.DS_Store" "*.git*" "webui/*"

if [ -d "$OUTPUT_DIR" ]; then
    cp -f "${ZIP_OUT}" "${OUTPUT_DIR}/${ZIP_OUT}"
    echo "=== Build Complete! Saved to ${OUTPUT_DIR}/${ZIP_OUT} ==="
else
    echo "=== Build Complete! Saved to ${ZIP_OUT} ==="
fi
