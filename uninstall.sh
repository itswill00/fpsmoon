#!/system/bin/sh
MODDIR="${0%/*}"

pkill -f "fpsmoon_daemon"  2>/dev/null
pkill -f "com.fpsmoon.FPSMoonOverlay" 2>/dev/null

rm -rf "$MODDIR/state/stats.json"
rm -rf "$MODDIR/state/daemon.log"
rm -rf "$MODDIR/state/overlay.log"
rm -rf "$MODDIR/state/stats.json.tmp"
rm -rf "$MODDIR/state/config.json.tmp"
rm -rf "$MODDIR/state/position.json.tmp"
