#!/system/bin/sh
MODDIR="${0%/*}"

echo "- Restarting FPS Moon services..."

mkdir -p "$MODDIR/state"
chmod 777 "$MODDIR/state"

# Kill existing instances safely
pkill -9 -f "fpsmoon_daemon" 2>/dev/null
pkill -9 -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null
sleep 0.5

export FPSMOON_STATE_DIR="$MODDIR/state"

# 1. Restart Native C Telemetry Daemon (detached)
if [ -f "$MODDIR/bin/fpsmoon_daemon" ]; then
    ( "$MODDIR/bin/fpsmoon_daemon" > "$MODDIR/state/daemon.log" 2>&1 & )
fi

# 2. Restart Native Java Overlay Engine (detached)
if [ -f "$MODDIR/bin/fpsmoon.dex" ]; then
    ( CLASSPATH="$MODDIR/bin/fpsmoon.dex" app_process /system/bin com.fpsmoon.FPSMoonOverlay "$MODDIR/state" > "$MODDIR/state/overlay.log" 2>&1 & )
fi

echo "- FPS Moon services restarted successfully."
