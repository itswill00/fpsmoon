#!/system/bin/sh
MODDIR="${0%/*}"

# Wait for Android System Boot Completion
until [ "$(getprop sys.boot_completed)" = "1" ]; do
    sleep 3
done
sleep 3

mkdir -p "$MODDIR/state"
chmod 777 "$MODDIR/state"

# Kill any stale instances
pkill -f "fpsmoon_daemon" 2>/dev/null
pkill -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null

# 1. Start Native C Telemetry Daemon
if [ -f "$MODDIR/bin/fpsmoon_daemon" ]; then
    ( "$MODDIR/bin/fpsmoon_daemon" > "$MODDIR/state/daemon.log" 2>&1 & )
fi

# 2. Start Native app_process Java Overlay Engine
if [ -f "$MODDIR/bin/fpsmoon.dex" ]; then
    ( CLASSPATH="$MODDIR/bin/fpsmoon.dex" app_process /system/bin com.fpsmoon.FPSMoonOverlay > "$MODDIR/state/overlay.log" 2>&1 & )
fi
