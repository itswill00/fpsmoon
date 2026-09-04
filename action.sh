#!/system/bin/sh
MODDIR="${0%/*}"

echo "- Restarting FPS Moon services..."

mkdir -p "$MODDIR/state"
chmod 777 "$MODDIR/state"
chcon -R u:object_r:system_file:s0 "$MODDIR/state" 2>/dev/null || true

# Grant SYSTEM_ALERT_WINDOW AppOps permission across AOSP, Custom ROMs, and OEMs
cmd appops set --uid 0 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set --uid 1000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set --uid 2000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set --uid 0 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set --uid 1000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set --uid 2000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set 0 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set 1000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set 2000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set root SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set android SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set android SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set com.android.shell SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set com.android.shell SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
pm grant com.android.shell android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null || true

# Kill existing instances safely
killall -9 fpsmoon_daemon 2>/dev/null || pkill -9 -x fpsmoon_daemon 2>/dev/null || true
for p in $(pgrep -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null); do
    if [ "$p" != "$$" ] && [ "$p" != "$PPID" ]; then
        kill -9 "$p" 2>/dev/null || true
    fi
done
sleep 0.5

export FPSMOON_STATE_DIR="$MODDIR/state"

# 1. Restart Native C Telemetry Daemon (detached)
if [ -f "$MODDIR/bin/fpsmoon_daemon" ]; then
    ( "$MODDIR/bin/fpsmoon_daemon" > "$MODDIR/state/daemon.log" 2>&1 & )
fi

# 2. Restart Native Java Overlay Engine (detached)
if [ -f "$MODDIR/bin/fpsmoon.dex" ]; then
    ( CLASSPATH="$MODDIR/bin/fpsmoon.dex" /system/bin/app_process /system/bin com.fpsmoon.FPSMoonOverlay "$MODDIR/state" > "$MODDIR/state/overlay.log" 2>&1 & )
fi

sleep 1

# Protect processes from Android LMK (Low Memory Killer) & Phantom Process Killer
for pid in $(pgrep -f "fpsmoon_daemon" 2>/dev/null); do
    echo -1000 > "/proc/$pid/oom_score_adj" 2>/dev/null || true
    chmod 000 "/proc/$pid/oom_score_adj" 2>/dev/null || true
done

for pid in $(pgrep -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null); do
    echo -1000 > "/proc/$pid/oom_score_adj" 2>/dev/null || true
    chmod 000 "/proc/$pid/oom_score_adj" 2>/dev/null || true
done

echo "- FPS Moon services restarted successfully."
