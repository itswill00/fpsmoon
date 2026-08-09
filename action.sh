#!/system/bin/sh
MODDIR="${0%/*}"

echo "- Restarting FPS Moon services..."

mkdir -p "$MODDIR/state"
chmod 777 "$MODDIR/state"
chcon -R u:object_r:system_file:s0 "$MODDIR/state" 2>/dev/null || true

# Grant SYSTEM_ALERT_WINDOW AppOps permission across OEMs (MIUI/HyperOS/ColorOS/OneUI)
appops set com.android.shell SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set com.android.shell SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
pm grant com.android.shell android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null || true
appops set android SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set android SYSTEM_ALERT_WINDOW allow 2>/dev/null || true

# Kill existing instances safely
pkill -9 -f "fpsmoon_daemon" 2>/dev/null
pkill -9 -f "fpsmoon_imgui" 2>/dev/null
sleep 0.5

export FPSMOON_STATE_DIR="$MODDIR/state"

# 1. Restart Native C Telemetry Daemon (detached)
if [ -f "$MODDIR/bin/fpsmoon_daemon" ]; then
    ( "$MODDIR/bin/fpsmoon_daemon" > "$MODDIR/state/daemon.log" 2>&1 & )
fi

# 2. Restart Native C++ Dear ImGui Overlay Engine (detached)
if [ -f "$MODDIR/bin/fpsmoon_imgui_launcher.dex" ] && [ -f "$MODDIR/bin/libfpsmoon_imgui.so" ]; then
    ( LD_LIBRARY_PATH="$MODDIR/bin:$LD_LIBRARY_PATH" CLASSPATH="$MODDIR/bin/fpsmoon_imgui_launcher.dex" app_process /system/bin com.fpsmoon.imgui.FPSMoonImGuiLauncher "$MODDIR/state" > "$MODDIR/state/imgui.log" 2>&1 & )
elif [ -f "$MODDIR/bin/fpsmoon_imgui" ]; then
    ( "$MODDIR/bin/fpsmoon_imgui" "$MODDIR/state" > "$MODDIR/state/imgui.log" 2>&1 & )
fi

sleep 1

# Protect processes from Android LMK (Low Memory Killer) & Phantom Process Killer
for pid in $(pgrep -f "fpsmoon_daemon" 2>/dev/null); do
    echo -1000 > "/proc/$pid/oom_score_adj" 2>/dev/null || true
    chmod 000 "/proc/$pid/oom_score_adj" 2>/dev/null || true
done

for pid in $(pgrep -f "FPSMoonImGuiLauncher" 2>/dev/null); do
    echo -1000 > "/proc/$pid/oom_score_adj" 2>/dev/null || true
    chmod 000 "/proc/$pid/oom_score_adj" 2>/dev/null || true
done

echo "- FPS Moon Native ImGui services restarted successfully."
