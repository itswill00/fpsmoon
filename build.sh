#!/system/bin/sh

PROJECT_DIR="/data/data/com.termux/files/home/fpsmoon"
OUTPUT_DIR="/sdcard/Download"

set -e

cd "$PROJECT_DIR"

if [ ! -f "module.prop" ]; then
    echo "error: module.prop not found"
    exit 1
fi

VERSION=$(grep '^version=' module.prop | cut -d= -f2)
VERSION_CODE=$(grep '^versionCode=' module.prop | cut -d= -f2)
ZIP_OUT="FPSMoon-${VERSION}.zip"

echo "building fpsmoon ${VERSION} (${VERSION_CODE})"

for tool in clang zip node npm; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        echo "error: $tool is not installed"
        exit 1
    fi
done

if [ -d "webui" ]; then
    if [ ! -d "webui/node_modules" ]; then
        if [ -d "/data/data/com.termux/files/home/HyperCore_Module/webui/node_modules" ]; then
            ln -s /data/data/com.termux/files/home/HyperCore_Module/webui/node_modules webui/node_modules
        else
            echo "installing webui dependencies..."
            (cd webui && npm install --no-audit --no-fund)
        fi
    fi
    
    echo "compiling webui..."
    if ! (cd webui && node ./node_modules/vite/bin/vite.js build); then
        echo "error: vite build failed"
        exit 1
    fi
    
    if [ ! -f "webui/dist/index.html" ]; then
        echo "error: webui output not found"
        exit 1
    fi
    
    mkdir -p webroot
    rm -f webroot/app.js webroot/styles.css
    cp webui/dist/index.html webroot/index.html
fi

echo "compiling c daemon..."
mkdir -p bin
clang -O3 -Wall -Wextra \
    src/fpsmoon_daemon.c \
    -o bin/fpsmoon_daemon
chmod 755 bin/fpsmoon_daemon

# Compile Java overlay if toolchain is available
ANDROID_JAR="/data/data/com.termux/files/home/android-sdk/android.jar"
if [ ! -f "$ANDROID_JAR" ]; then
    ANDROID_JAR=$(find /data/data/com.termux/files/home -name "android.jar" 2>/dev/null | head -n 1)
fi

if [ -n "$ANDROID_JAR" ] && command -v ecj >/dev/null 2>&1 && (command -v dx >/dev/null 2>&1 || command -v d8 >/dev/null 2>&1); then
    echo "compiling java overlay..."
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
    echo "compiled bin/fpsmoon.dex successfully"
fi
chmod 644 bin/fpsmoon.dex

mkdir -p "$OUTPUT_DIR"
rm -f "$OUTPUT_DIR/FPSMoon-${VERSION}"*.zip

echo "packaging zip package..."
if ! zip -r "$OUTPUT_DIR/$ZIP_OUT" \
    action.sh \
    bin/fpsmoon_daemon \
    bin/fpsmoon.dex \
    customize.sh \
    module.prop \
    service.sh \
    state/config.json \
    state/position.json \
    uninstall.sh \
    webroot/index.html \
    README.md \
    LICENSE >/dev/null; then
    echo "error: packaging failed"
    exit 1
fi

am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d "file://$OUTPUT_DIR/$ZIP_OUT" >/dev/null 2>&1 || true

echo "build finished: ${OUTPUT_DIR}/${ZIP_OUT}"

if [ "$1" = "--deploy" ] || [ "$1" = "-d" ]; then
    echo "deploying to live device modules..."
    MOD_TARGET="/data/adb/modules/fps_moon"
    if su -c "[ -d $MOD_TARGET ]"; then
        su -c "mkdir -p $MOD_TARGET/bin $MOD_TARGET/webroot $MOD_TARGET/state"
        su -c "cp $PROJECT_DIR/bin/fpsmoon_daemon $MOD_TARGET/bin/fpsmoon_daemon"
        su -c "cp $PROJECT_DIR/bin/fpsmoon.dex $MOD_TARGET/bin/fpsmoon.dex"
        su -c "rm -f $MOD_TARGET/webroot/app.js $MOD_TARGET/webroot/styles.css"
        su -c "cp $PROJECT_DIR/webroot/index.html $MOD_TARGET/webroot/index.html"
        su -c "cp $PROJECT_DIR/module.prop $MOD_TARGET/module.prop"
        su -c "cp $PROJECT_DIR/action.sh $MOD_TARGET/action.sh"
        su -c "cp $PROJECT_DIR/service.sh $MOD_TARGET/service.sh"
        su -c "cp $PROJECT_DIR/customize.sh $MOD_TARGET/customize.sh"
        su -c "cp $PROJECT_DIR/uninstall.sh $MOD_TARGET/uninstall.sh"
        su -c "chmod 755 $MOD_TARGET/bin/* $MOD_TARGET/action.sh $MOD_TARGET/service.sh $MOD_TARGET/customize.sh $MOD_TARGET/uninstall.sh"
        su -c "chmod 644 $MOD_TARGET/bin/fpsmoon.dex $MOD_TARGET/webroot/index.html $MOD_TARGET/module.prop"
        su -c "chmod 777 $MOD_TARGET/state"
        su -c "sh $MOD_TARGET/action.sh"
        echo "module deployed and services restarted successfully"
    else
        echo "error: $MOD_TARGET not found, flash the module zip first"
        exit 1
    fi
fi
