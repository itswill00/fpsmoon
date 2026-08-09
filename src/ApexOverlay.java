package com.apexhud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApexOverlay {
    private static Context context;
    private static WindowManager windowManager;
    private static LinearLayout hudView;
    private static WindowManager.LayoutParams params;
    private static Handler handler;

    // View Components
    private static TextView tvFps;
    private static TextView tvCpu;
    private static TextView tvGpu;
    private static TextView tvRam;
    private static TextView tvBattery;

    // Config & State Paths
    private static final String STATE_PATH = "/data/adb/modules/apex_hud/state/stats.json";
    private static final String CONFIG_PATH = "/data/adb/modules/apex_hud/state/config.json";
    private static final String POS_PATH = "/data/adb/modules/apex_hud/state/position.json";

    // Layout Params
    private static int posX = 30;
    private static int posY = 120;
    private static float scale = 1.0f;
    private static float opacity = 0.85f;
    private static String theme = "cyber_neon"; // cyber_neon, amoled, matrix, crimson, amber
    private static boolean isVisible = true;

    // Toggles
    private static boolean showFps = true;
    private static boolean showCpu = true;
    private static boolean showGpu = true;
    private static boolean showRam = true;
    private static boolean showBattery = true;

    public static void main(String[] args) {
        System.out.println("[ApexHUD] Starting Overlay Process...");

        try {
            Looper.prepareMainLooper();

            // Initialize System Context via reflection
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getMethod("systemMain");
            Object activityThread = systemMainMethod.invoke(null);
            Method getSystemContextMethod = activityThreadClass.getMethod("getSystemContext");
            context = (Context) getSystemContextMethod.invoke(activityThread);

            if (context == null) {
                System.err.println("[ApexHUD] Error: Unable to get system context.");
                return;
            }

            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            handler = new Handler(Looper.getMainLooper());

            // Build HUD UI
            createHudWindow();

            // Load initial saved position
            readPosition();

            // Start update loop (every 350ms)
            startLoop();

            System.out.println("[ApexHUD] Overlay service successfully attached to WindowManager.");
            Looper.loop();
        } catch (Exception e) {
            System.err.println("[ApexHUD] Exception in Overlay main:");
            e.printStackTrace();
        }
    }

    private static void createHudWindow() {
        hudView = new LinearLayout(context);
        hudView.setOrientation(LinearLayout.VERTICAL);
        hudView.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        // Background styling
        applyBackgroundStyle();

        // Initialize Text Elements
        tvFps = createTextView(16, true);
        tvCpu = createTextView(11, false);
        tvGpu = createTextView(11, false);
        tvRam = createTextView(11, false);
        tvBattery = createTextView(11, false);

        hudView.addView(tvFps);
        hudView.addView(tvCpu);
        hudView.addView(tvGpu);
        hudView.addView(tvRam);
        hudView.addView(tvBattery);

        // Window Layout Parameters (TYPE_APPLICATION_OVERLAY = 2038)
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                2038, // TYPE_APPLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = posX;
        params.y = posY;

        // Touch listener for dragging HUD around screen
        hudView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        try {
                            windowManager.updateViewLayout(hudView, params);
                        } catch (Exception ignored) {}
                        return true;

                    case MotionEvent.ACTION_UP:
                        posX = params.x;
                        posY = params.y;
                        savePosition();
                        return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(hudView, params);
        } catch (Exception e) {
            System.err.println("[ApexHUD] Failed to add Window View directly, trying fallback flags...");
            params.type = 2003; // TYPE_SYSTEM_ALERT fallback
            windowManager.addView(hudView, params);
        }
    }

    private static TextView createTextView(float sizeSp, boolean isBold) {
        TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        tv.setTextColor(Color.WHITE);
        if (isBold) {
            tv.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            tv.setTypeface(Typeface.MONOSPACE);
        }
        tv.setShadowLayer(3, 0, 0, Color.BLACK);
        return tv;
    }

    private static void applyBackgroundStyle() {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dpToPx(10));

        int bgColor;
        int strokeColor;

        if ("amoled".equalsIgnoreCase(theme)) {
            bgColor = Color.argb((int)(opacity * 255), 0, 0, 0);
            strokeColor = Color.argb(180, 80, 80, 80);
        } else if ("matrix".equalsIgnoreCase(theme)) {
            bgColor = Color.argb((int)(opacity * 255), 5, 25, 10);
            strokeColor = Color.argb(200, 0, 255, 120);
        } else if ("crimson".equalsIgnoreCase(theme)) {
            bgColor = Color.argb((int)(opacity * 255), 25, 5, 10);
            strokeColor = Color.argb(200, 255, 40, 80);
        } else if ("amber".equalsIgnoreCase(theme)) {
            bgColor = Color.argb((int)(opacity * 255), 25, 18, 5);
            strokeColor = Color.argb(200, 255, 180, 0);
        } else {
            // Cyber Neon Default (Cyan/Purple tint)
            bgColor = Color.argb((int)(opacity * 255), 10, 15, 30);
            strokeColor = Color.argb(200, 0, 225, 255);
        }

        shape.setColor(bgColor);
        shape.setStroke(dpToPx(1), strokeColor);
        hudView.setBackground(shape);
    }

    private static int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private static void startLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateHudData();
                handler.postDelayed(this, 350);
            }
        }, 350);
    }

    private static void updateHudData() {
        // Read config and stats
        readConfig();
        Map<String, String> stats = readStats();

        if (!isVisible) {
            hudView.setVisibility(View.GONE);
            return;
        } else {
            hudView.setVisibility(View.VISIBLE);
        }

        applyBackgroundStyle();

        // 1. FPS & Frametime
        if (showFps) {
            String fpsStr = stats.getOrDefault("fps", "60");
            String ftStr = stats.getOrDefault("frametime", "16.6");
            tvFps.setVisibility(View.VISIBLE);
            tvFps.setText("FPS: " + fpsStr + " (" + ftStr + "ms)");

            try {
                int fpsVal = Integer.parseInt(fpsStr);
                if (fpsVal >= 55) tvFps.setTextColor(Color.parseColor("#00FF88"));
                else if (fpsVal >= 30) tvFps.setTextColor(Color.parseColor("#FFD000"));
                else tvFps.setTextColor(Color.parseColor("#FF3366"));
            } catch (Exception e) {
                tvFps.setTextColor(Color.WHITE);
            }
        } else {
            tvFps.setVisibility(View.GONE);
        }

        // 2. CPU
        if (showCpu) {
            String temp = stats.getOrDefault("cpu_temp", "--");
            String freq = stats.getOrDefault("cpu_freq", "--");
            String load = stats.getOrDefault("cpu_load", "--");
            tvCpu.setVisibility(View.VISIBLE);
            tvCpu.setText("CPU: " + load + "% | " + freq + " | " + temp + "°C");
        } else {
            tvCpu.setVisibility(View.GONE);
        }

        // 3. GPU
        if (showGpu) {
            String load = stats.getOrDefault("gpu_load", "--");
            String temp = stats.getOrDefault("gpu_temp", "--");
            tvGpu.setVisibility(View.VISIBLE);
            tvGpu.setText("GPU: " + load + "% | " + temp + "°C");
        } else {
            tvGpu.setVisibility(View.GONE);
        }

        // 4. RAM
        if (showRam) {
            String ramUsed = stats.getOrDefault("ram_used", "--");
            String ramTotal = stats.getOrDefault("ram_total", "--");
            tvRam.setVisibility(View.VISIBLE);
            tvRam.setText("RAM: " + ramUsed + " / " + ramTotal + " GB");
        } else {
            tvRam.setVisibility(View.GONE);
        }

        // 5. Battery
        if (showBattery) {
            String watt = stats.getOrDefault("bat_watt", "--");
            String temp = stats.getOrDefault("bat_temp", "--");
            tvBattery.setVisibility(View.VISIBLE);
            tvBattery.setText("PWR: " + watt + "W | " + temp + "°C");
        } else {
            tvBattery.setVisibility(View.GONE);
        }
    }

    private static Map<String, String> readStats() {
        Map<String, String> map = new HashMap<>();
        File file = new File(STATE_PATH);
        if (!file.exists()) return map;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim().replace("\"", "").replace(",", "");
                    String val = parts[1].trim().replace("\"", "").replace(",", "");
                    map.put(key, val);
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private static void readConfig() {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim().replace("\"", "").replace(",", "");
                    String val = parts[1].trim().replace("\"", "").replace(",", "");

                    switch (key) {
                        case "visible": isVisible = Boolean.parseBoolean(val); break;
                        case "show_fps": showFps = Boolean.parseBoolean(val); break;
                        case "show_cpu": showCpu = Boolean.parseBoolean(val); break;
                        case "show_gpu": showGpu = Boolean.parseBoolean(val); break;
                        case "show_ram": showRam = Boolean.parseBoolean(val); break;
                        case "show_battery": showBattery = Boolean.parseBoolean(val); break;
                        case "theme": theme = val; break;
                        case "opacity": opacity = Float.parseFloat(val); break;
                        case "scale": scale = Float.parseFloat(val); break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void readPosition() {
        File file = new File(POS_PATH);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim().replace("\"", "").replace(",", "");
                    int val = Integer.parseInt(parts[1].trim().replace("\"", "").replace(",", ""));
                    if ("x".equals(key)) posX = val;
                    if ("y".equals(key)) posY = val;
                }
            }
            if (params != null) {
                params.x = posX;
                params.y = posY;
                windowManager.updateViewLayout(hudView, params);
            }
        } catch (Exception ignored) {}
    }

    private static void savePosition() {
        try (FileWriter fw = new FileWriter(POS_PATH)) {
            fw.write("{\n  \"x\": " + posX + ",\n  \"y\": " + posY + "\n}\n");
        } catch (Exception ignored) {}
    }
}
