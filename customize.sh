#!/system/bin/sh
SKIPUNZIP=0

ui_print "Installing FPS Moon..."

mkdir -p "$MODPATH/state"
chmod 777 "$MODPATH/state"

if [ ! -f "$MODPATH/state/config.json" ]; then
    cat > "$MODPATH/state/config.json" << 'EOF'
{
  "visible": true,
  "show_fps": true,
  "show_cpu": true,
  "show_gov": false,
  "show_gpu": true,
  "show_gpu_gov": false,
  "show_ram": false,
  "show_zram": false,
  "show_battery": true,
  "show_net": false,
  "is_horizontal": true,
  "align": "left",
  "theme": "cyber_neon",
  "custom_color": "#6366F1",
  "opacity": 0.85,
  "scale": 1.0,
  "font_size": 12,
  "corner_radius": 14,
  "bg_width": 250,
  "bg_height": 56,
  "refresh_interval": 250,
  "target_fps": 60
}
EOF
fi

if [ ! -f "$MODPATH/state/position.json" ]; then
    echo '{"x": 30, "y": 250}' > "$MODPATH/state/position.json"
fi

chmod 755 "$MODPATH/bin/fpsmoon_daemon"
chmod 755 "$MODPATH/service.sh"
chmod 755 "$MODPATH/action.sh"
chmod 755 "$MODPATH/uninstall.sh"

ui_print "FPS Moon installed successfully."
