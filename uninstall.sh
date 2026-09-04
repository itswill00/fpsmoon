#!/system/bin/sh
MODDIR="${0%/*}"

# Terminate running daemon and overlay instances safely
killall -9 fpsmoon_daemon 2>/dev/null || pkill -9 -x fpsmoon_daemon 2>/dev/null || true
for p in $(pgrep -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null); do
    if [ "$p" != "$$" ] && [ "$p" != "$PPID" ]; then
        kill -9 "$p" 2>/dev/null || true
    fi
done

rm -rf "$MODDIR/state"
