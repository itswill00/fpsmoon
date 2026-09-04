#!/system/bin/sh
SKIPUNZIP=0

ui_print "- Installing FPS Moon..."

ARCH_ABI=$(getprop ro.product.cpu.abi 2>/dev/null)
if [ "$ARCH" != "arm64" ] && [ "$ARCH_ABI" != "arm64-v8a" ]; then
    abort "! Requires ARM64 device"
fi

mkdir -p "$MODPATH/state"
chmod 777 "$MODPATH/state"

# Preserve user settings on upgrade
PREV_STATE="/data/adb/modules/fps_moon/state"
if [ -d "$PREV_STATE" ]; then
    cp -af "$PREV_STATE/config.json" "$MODPATH/state/config.json" 2>/dev/null || true
    cp -af "$PREV_STATE/position.json" "$MODPATH/state/position.json" 2>/dev/null || true
fi

# Fallback config
if [ ! -f "$MODPATH/state/config.json" ]; then
    cat > "$MODPATH/state/config.json" << 'EOF'
{
  "visible": true,
  "show_fps": true,
  "show_cpu": true,
  "show_cpu_freq": true,
  "show_gov": true,
  "show_gpu": true,
  "show_gpu_freq": true,
  "show_gpu_gov": true,
  "show_ram": true,
  "show_zram": false,
  "show_battery": true,
  "show_net": false,
  "is_horizontal": false,
  "align": "left",
  "theme": "cyber_neon",
  "custom_color": "#6366F1",
  "opacity": 0.58,
  "scale": 0.79,
  "font_size": 11,
  "corner_radius": 14,
  "bg_width": 150,
  "bg_height": 160,
  "refresh_interval": 850,
  "target_fps": 60
}
EOF
fi

if [ ! -f "$MODPATH/state/position.json" ]; then
    echo '{"x": 565, "y": 156}' > "$MODPATH/state/position.json"
fi

# Permissions
chmod 755 "$MODPATH/bin/fpsmoon_daemon"
chmod 644 "$MODPATH/bin/fpsmoon.dex"
chmod 755 "$MODPATH/service.sh"
chmod 755 "$MODPATH/action.sh"
chmod 755 "$MODPATH/uninstall.sh"
chmod 644 "$MODPATH/webroot/index.html" 2>/dev/null || true
chmod 644 "$MODPATH/module.prop"

# Overlay permissions
cmd appops set --uid 0 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set --uid 1000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
cmd appops set --uid 2000 SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set android SYSTEM_ALERT_WINDOW allow 2>/dev/null || true
appops set com.android.shell SYSTEM_ALERT_WINDOW allow 2>/dev/null || true

ui_print "- Done. Configure via WebUI."
