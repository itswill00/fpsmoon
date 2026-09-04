#!/system/bin/sh
SKIPUNZIP=0

ui_print "*****************************************"
ui_print "*               FPS Moon                *"
ui_print "* Real-time Performance HUD & Overlay   *"
ui_print "*             by @itswill00             *"
ui_print "*****************************************"

# Architecture verification
ARCH_ABI=$(getprop ro.product.cpu.abi 2>/dev/null)
if [ "$ARCH" != "arm64" ] && [ "$ARCH_ABI" != "arm64-v8a" ]; then
    ui_print "! Unsupported architecture: ${ARCH_ABI:-$ARCH}"
    ui_print "! FPS Moon requires an ARM64 (arm64-v8a) device."
    abort "! Installation aborted"
fi

ui_print "- Device: $(getprop ro.product.brand) $(getprop ro.product.model)"
ui_print "- Android: $(getprop ro.build.version.release)"
ui_print "- Kernel: $(uname -r)"

mkdir -p "$MODPATH/state"
chmod 777 "$MODPATH/state"

# Preserve existing user settings if upgrading
PREV_STATE="/data/adb/modules/fps_moon/state"
if [ -d "$PREV_STATE" ]; then
    if [ -f "$PREV_STATE/config.json" ]; then
        ui_print "- Preserving user configuration..."
        cp -af "$PREV_STATE/config.json" "$MODPATH/state/config.json" 2>/dev/null || true
    fi
    if [ -f "$PREV_STATE/position.json" ]; then
        ui_print "- Preserving overlay screen position..."
        cp -af "$PREV_STATE/position.json" "$MODPATH/state/position.json" 2>/dev/null || true
    fi
fi

# Fallback to defaults if clean install
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

# Set permissions
chmod 755 "$MODPATH/bin/fpsmoon_daemon"
chmod 644 "$MODPATH/bin/fpsmoon.dex"
chmod 755 "$MODPATH/service.sh"
chmod 755 "$MODPATH/action.sh"
chmod 755 "$MODPATH/uninstall.sh"
chmod 644 "$MODPATH/webroot/index.html" 2>/dev/null || true
chmod 644 "$MODPATH/module.prop"

# Pre-grant overlay permissions for AOSP, Custom ROMs, and OEMs
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

ui_print "- Pre-granted window manager overlay permissions."
ui_print "- Single-file WebUI integrated."
ui_print "*****************************************"
ui_print "* Installation complete!                *"
ui_print "* Access WebUI in KernelSU / APatch /   *"
ui_print "* Magisk Manager to customize HUD.      *"
ui_print "*****************************************"
