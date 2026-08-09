package com.fpsmoon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FPSMoonOverlay {
    private static Context context;
    private static WindowManager windowManager;
    private static CanvasHudView hudView;
    private static WindowManager.LayoutParams params;
    private static Handler handler;

    // Config & State Paths
    private static String stateDir   = "/data/adb/modules/fps_moon/state";
    private static String statePath  = stateDir + "/stats.json";
    private static String configPath = stateDir + "/config.json";
    private static String posPath    = stateDir + "/position.json";

    // Layout State Variables
    private static int posX = 60;
    private static int posY = 250;
    private static float scale = 1.0f;
    private static float opacity = 0.85f;
    private static int fontSizeSp = 12;
    private static int cornerRadiusDp = 14;
    private static int bgWidthDp = 260;
    private static int bgHeightDp = 58;
    private static int refreshInterval = 250;
    private static String theme = "cyber_neon";
    private static String customHexColor = "#6366F1";
    private static boolean isHorizontal = true;
    private static String align = "left"; // left, center, right
    private static boolean isVisible = true;

    // Config timestamp check — instant sync on file modification
    private static long lastConfigModified = 0L;

    // Metric Toggles
    private static boolean showFps = true;
    private static boolean showCpu = true;
    private static boolean showGov = false;
    private static boolean showGpu = true;
    private static boolean showGpuGov = false;
    private static boolean showRam = false;
    private static boolean showZram = false;
    private static boolean showBattery = true;
    private static boolean showNet = false;

    // Render Stats State
    private static String fpsText = "60";
    private static String ftText = "16.6";
    private static String hzText = "60Hz";
    private static String cpuText = "CPU 45°C (35%)";
    private static String govText = "schedutil";
    private static String gpuText = "GPU 72%";
    private static String gpuGovText = "ged_dvfs";
    private static String ramText = "RAM 4.7/7.5G";
    private static String zramText = "ZRAM 1.8/6.0G";
    private static String pwrText = "PWR 3.45W";
    private static String netText = "DL 0 KB/s";

    static class CanvasHudView extends View {
        private Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintFpsNum = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintFpsLbl = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintSubText = new Paint(Paint.ANTI_ALIAS_FLAG);
        private RectF rectBg = new RectF();

        public CanvasHudView(Context context) {
            super(context);
            paintBg.setStyle(Paint.Style.FILL);
            paintFpsNum.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
            paintFpsLbl.setColor(Color.parseColor("#94A3B8"));
            paintFpsLbl.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            paintSubText.setColor(Color.parseColor("#E2E8F0"));
            paintSubText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int[] dims = calcHudDimensions();
            int measuredW = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED
                    ? MeasureSpec.getSize(widthMeasureSpec) : dims[0];
            int measuredH = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED
                    ? MeasureSpec.getSize(heightMeasureSpec) : dims[1];
            setMeasuredDimension(measuredW, measuredH);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            try {
                int[] dims = calcHudDimensions();
                float cardW = getWidth() > 0 ? getWidth() : dims[0];
                float cardH = getHeight() > 0 ? getHeight() : dims[1];

                // 1. Draw Glassmorphism Background Card (Solid & Consistent)
                int bgColor;
                if ("amoled".equalsIgnoreCase(theme)) {
                    bgColor = Color.argb((int)(opacity * 255), 0, 0, 0);
                } else if ("matrix".equalsIgnoreCase(theme)) {
                    bgColor = Color.argb((int)(opacity * 255), 6, 22, 14);
                } else if ("crimson".equalsIgnoreCase(theme)) {
                    bgColor = Color.argb((int)(opacity * 255), 24, 8, 12);
                } else if ("custom".equalsIgnoreCase(theme) && customHexColor != null && customHexColor.startsWith("#")) {
                    try {
                        int c = Color.parseColor(customHexColor);
                        bgColor = Color.argb((int)(opacity * 255), Color.red(c), Color.green(c), Color.blue(c));
                    } catch (Exception e) {
                        bgColor = Color.argb((int)(opacity * 255), 11, 13, 20);
                    }
                } else {
                    bgColor = Color.argb((int)(opacity * 255), 11, 13, 20);
                }

                paintBg.setColor(bgColor);
                float r = dpToPx(cornerRadiusDp) * scale;
                rectBg.set(0, 0, cardW, cardH);
                canvas.drawRoundRect(rectBg, r, r, paintBg);

                // 2. Consistent Sleek FPS Text Color (No border, No jarring color shift during games)
                String drawFps = (fpsText != null) ? fpsText : "0";
                if ("matrix".equalsIgnoreCase(theme)) {
                    paintFpsNum.setColor(Color.parseColor("#22C55E"));
                } else if ("crimson".equalsIgnoreCase(theme)) {
                    paintFpsNum.setColor(Color.parseColor("#F43F5E"));
                } else {
                    paintFpsNum.setColor(Color.parseColor("#38BDF8"));
                }

                // 3. Dynamic Alignment & Padding Calculation
                float padX;
                Paint.Align paintAlign;

                if ("center".equalsIgnoreCase(align)) {
                    padX = cardW / 2.0f;
                    paintAlign = Paint.Align.CENTER;
                } else if ("right".equalsIgnoreCase(align)) {
                    padX = cardW - (dpToPx(14) * scale);
                    paintAlign = Paint.Align.RIGHT;
                } else {
                    padX = dpToPx(14) * scale;
                    paintAlign = Paint.Align.LEFT;
                }

                paintFpsNum.setTextAlign(paintAlign);
                paintFpsLbl.setTextAlign(paintAlign);
                paintSubText.setTextAlign(paintAlign);

                String drawFt = (ftText != null) ? ftText : "0.0";
                String drawHz = (hzText != null) ? hzText : "60Hz";

                float fpsNumSize = spToPx(fontSizeSp + 7) * scale;
                float subTextSize = spToPx(Math.max(9, fontSizeSp - 1)) * scale;

                paintFpsNum.setTextSize(fpsNumSize);
                paintFpsLbl.setTextSize(subTextSize);
                paintSubText.setTextSize(subTextSize);

                if (isHorizontal) {
                    float totalTextH = fpsNumSize + (dpToPx(4) * scale) + subTextSize;
                    float startY = (cardH - totalTextH) / 2.0f;
                    float yRow1 = startY + (fpsNumSize * 0.82f);
                    float yRow2 = yRow1 + (dpToPx(4) * scale) + (subTextSize * 0.82f);

                    if ("center".equalsIgnoreCase(align) || "right".equalsIgnoreCase(align)) {
                        String fullLine = drawFps + " FPS (" + drawFt + " ms) [" + drawHz + "]";
                        canvas.drawText(fullLine, padX, yRow1, paintFpsNum);
                    } else {
                        canvas.drawText(drawFps, padX, yRow1, paintFpsNum);
                        float fpsWidth = paintFpsNum.measureText(drawFps);
                        canvas.drawText(" FPS  (" + drawFt + " ms) [" + drawHz + "]", padX + fpsWidth, yRow1, paintFpsLbl);
                    }

                    // Row 2 Hardware Metrics Chips
                    StringBuilder sbChips = new StringBuilder();
                    if (showCpu && cpuText != null) sbChips.append(cpuText);
                    if (showGov && govText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(govText);
                    if (showGpu && gpuText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(gpuText);
                    if (showGpuGov && gpuGovText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append("GPUGov: ").append(gpuGovText);
                    if (showRam && ramText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(ramText);
                    if (showZram && zramText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(zramText);
                    if (showBattery && pwrText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(pwrText);
                    if (showNet && netText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(netText);

                    canvas.drawText(sbChips.toString(), padX, yRow2, paintSubText);

                } else {
                    // Vertical Stack Layout with Dynamic Line Spacing
                    float lineGap = subTextSize * 1.45f;
                    float yCurr = (dpToPx(16) * scale) + fpsNumSize * 0.75f;

                    canvas.drawText(drawFps + " FPS", padX, yCurr, paintFpsNum);

                    yCurr += lineGap;
                    canvas.drawText(drawFt + " ms  [" + drawHz + "]", padX, yCurr, paintFpsLbl);

                    if (showCpu && cpuText != null) {
                        yCurr += lineGap;
                        canvas.drawText(cpuText, padX, yCurr, paintSubText);
                    }
                    if (showGov && govText != null) {
                        yCurr += lineGap;
                        canvas.drawText("CPUGov: " + govText, padX, yCurr, paintSubText);
                    }
                    if (showGpu && gpuText != null) {
                        yCurr += lineGap;
                        canvas.drawText(gpuText, padX, yCurr, paintSubText);
                    }
                    if (showGpuGov && gpuGovText != null) {
                        yCurr += lineGap;
                        canvas.drawText("GPUGov: " + gpuGovText, padX, yCurr, paintSubText);
                    }
                    if (showRam && ramText != null) {
                        yCurr += lineGap;
                        canvas.drawText(ramText, padX, yCurr, paintSubText);
                    }
                    if (showZram && zramText != null) {
                        yCurr += lineGap;
                        canvas.drawText(zramText, padX, yCurr, paintSubText);
                    }
                    if (showBattery && pwrText != null) {
                        yCurr += lineGap;
                        canvas.drawText(pwrText, padX, yCurr, paintSubText);
                    }
                    if (showNet && netText != null) {
                        yCurr += lineGap;
                        canvas.drawText(netText, padX, yCurr, paintSubText);
                    }
                }
            } catch (Throwable t) {
                // Fail-safe protection against canvas rendering crashes
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("[FPS Moon] Starting overlay...");

        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            stateDir = args[0].trim();
        } else {
            String envState = System.getenv("FPSMOON_STATE_DIR");
            if (envState != null && !envState.trim().isEmpty()) {
                stateDir = envState.trim();
            }
        }
        statePath  = stateDir + "/stats.json";
        configPath = stateDir + "/config.json";
        posPath    = stateDir + "/position.json";

        try {
            Looper.prepareMainLooper();

            try {
                Method fontMapMethod = Typeface.class.getDeclaredMethod("loadPreinstalledSystemFontMap");
                fontMapMethod.setAccessible(true);
                fontMapMethod.invoke(null);
            } catch (Throwable ignored) {}

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getMethod("systemMain");
            Object activityThread = systemMainMethod.invoke(null);
            Context sysContext = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);

            if (sysContext == null) {
                System.err.println("[FPS Moon] Unable to acquire system context.");
                return;
            }

            DisplayManager dm = (DisplayManager) sysContext.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
            context = sysContext.createDisplayContext(defaultDisplay);

            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            handler = new Handler(Looper.getMainLooper());

            // Register Graceful WindowManager Surface Cleanup Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (windowManager != null && hudView != null) {
                        try {
                            windowManager.removeView(hudView);
                        } catch (Exception ignored) {}
                    }
                }
            }));

            readConfig();
            readPosition();
            createHudWindow();
            startLoop();

            System.out.println("[FPS Moon] Overlay started successfully.");

            while (true) {
                try {
                    Looper.loop();
                } catch (Throwable t) {
                    System.err.println("[FPS Moon] Looper crash: " + t.getMessage());
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[FPS Moon] Error in overlay:");
            e.printStackTrace();
        }
    }

    // Reusable Measurement Paint Objects & Result Array (Zero Heap GC Overhead)
    private static final Paint measurePaintNum = new Paint();
    private static final Paint measurePaintSub = new Paint();
    private static final int[] dimsResult = new int[2];
    private static final Pattern STATS_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?[\\d.]+|(?:true|false)))");

    /** Single source of truth for smart HUD dimensions (Width and Height in Px) */
    private static int[] calcHudDimensions() {
        if (context == null) {
            dimsResult[0] = 400;
            dimsResult[1] = 100;
            return dimsResult;
        }

        float fpsNumSize = spToPx(fontSizeSp + 7) * scale;
        float subTextSize = spToPx(Math.max(9, fontSizeSp - 1)) * scale;

        measurePaintNum.setTextSize(fpsNumSize);
        measurePaintNum.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

        measurePaintSub.setTextSize(subTextSize);
        measurePaintSub.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        String drawFps = (fpsText != null) ? fpsText : "0";
        String drawFt = (ftText != null) ? ftText : "0.0";
        String drawHz = (hzText != null) ? hzText : "60Hz";

        float maxContentW = 0f;

        if (isHorizontal) {
            float row1W = measurePaintNum.measureText(drawFps) + measurePaintSub.measureText(" FPS  (" + drawFt + " ms) [" + drawHz + "]");

            StringBuilder sbChips = new StringBuilder();
            if (showCpu && cpuText != null) sbChips.append(cpuText);
            if (showGov && govText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(govText);
            if (showGpu && gpuText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(gpuText);
            if (showGpuGov && gpuGovText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append("GPUGov: ").append(gpuGovText);
            if (showRam && ramText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(ramText);
            if (showZram && zramText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(zramText);
            if (showBattery && pwrText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(pwrText);
            if (showNet && netText != null) sbChips.append(sbChips.length() > 0 ? " • " : "").append(netText);

            float row2W = measurePaintSub.measureText(sbChips.toString());
            maxContentW = Math.max(row1W, row2W);
        } else {
            maxContentW = Math.max(
                    measurePaintNum.measureText(drawFps + " FPS"),
                    measurePaintSub.measureText(drawFt + " ms [" + drawHz + "]")
            );

            if (showCpu && cpuText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(cpuText));
            if (showGov && govText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText("CPUGov: " + govText));
            if (showGpu && gpuText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(gpuText));
            if (showGpuGov && gpuGovText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText("GPUGov: " + gpuGovText));
            if (showRam && ramText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(ramText));
            if (showZram && zramText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(zramText));
            if (showBattery && pwrText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(pwrText));
            if (showNet && netText != null) maxContentW = Math.max(maxContentW, measurePaintSub.measureText(netText));
        }

        // Add 28dp padding for left + right
        float paddingX = dpToPx(28) * scale;
        int requiredWidthPx = Math.round(maxContentW + paddingX);
        int targetWidthPx = Math.max((int)(dpToPx(bgWidthDp) * scale), requiredWidthPx);

        // Height calculation: Directly obey user bgHeightDp slider!
        int targetHeightPx = Math.max((int)(dpToPx(24) * scale), (int)(dpToPx(bgHeightDp) * scale));

        dimsResult[0] = targetWidthPx;
        dimsResult[1] = targetHeightPx;
        return dimsResult;
    }

    private static void createHudWindow() {
        hudView = new CanvasHudView(context);

        if (posY < 160) posY = 250;

        int[] dims = calcHudDimensions();
        int initialW = dims[0];
        int initialH = dims[1];

        params = new WindowManager.LayoutParams(
                initialW,
                initialH,
                2038, // TYPE_APPLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = posX;
        params.y = posY;

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
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);

                        DisplayMetrics dm = context.getResources().getDisplayMetrics();
                        if (newX < 0) newX = 0;
                        if (newY < 160) newY = 160;  // unified boundary
                        if (newX > dm.widthPixels - 50) newX = dm.widthPixels - 50;
                        if (newY > dm.heightPixels - 50) newY = dm.heightPixels - 50;

                        if (Math.abs(newX - params.x) >= 2 || Math.abs(newY - params.y) >= 2) {
                            params.x = newX;
                            params.y = newY;
                            try {
                                windowManager.updateViewLayout(hudView, params);
                            } catch (Exception ignored) {}
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        posX = params.x;
                        posY = params.y;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                savePosition();
                            }
                        });
                        return true;
                }
                return false;
            }
        });

        int[] windowTypes = new int[]{ 2038, 2034, 2015, 2003, 2002 };
        for (int type : windowTypes) {
            try {
                params.type = type;
                windowManager.addView(hudView, params);
                System.out.println("[FPS Moon] Attached to layer " + type);
                break;
            } catch (Exception ignored) {}
        }
    }

    private static int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private static int spToPx(float sp) {
        return Math.round(sp * context.getResources().getDisplayMetrics().scaledDensity);
    }

    private static void startLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    updateHudData();
                } catch (Throwable ignored) {}
                int delay = Math.max(50, refreshInterval);
                handler.postDelayed(this, delay);
            }
        }, Math.max(50, refreshInterval));
    }

    private static void updateHudData() {
        // Instant config sync when config file is modified
        File cfgFile = new File(configPath);
        if (cfgFile.exists()) {
            long modTime = cfgFile.lastModified();
            if (modTime != lastConfigModified) {
                readConfig();
                lastConfigModified = modTime;
            }
        }

        Map<String, String> stats = readStats();

        if (!isVisible) {
            hudView.setVisibility(View.GONE);
            return;
        } else {
            hudView.setVisibility(View.VISIBLE);
        }

        fpsText = stats.getOrDefault("fps", "60");
        ftText  = stats.getOrDefault("frametime", "16.6");
        hzText  = stats.getOrDefault("screen_hz", "60Hz");

        String cTemp = stats.getOrDefault("cpu_temp", "--");
        String cLoad = stats.getOrDefault("cpu_load", "--");
        cpuText = "CPU " + cTemp + "°C (" + cLoad + "%)";

        govText = stats.getOrDefault("cpu_gov", "schedutil");

        String gLoad = stats.getOrDefault("gpu_load", "--");
        String gTemp = stats.getOrDefault("gpu_temp", "--");
        gpuText = "GPU " + gTemp + "°C (" + gLoad + "%)";

        gpuGovText = stats.getOrDefault("gpu_gov", "ged_dvfs");

        String rUsed  = stats.getOrDefault("ram_used",  "--");
        String rTotal = stats.getOrDefault("ram_total", "--");
        ramText = "RAM " + rUsed + "/" + rTotal + "GB";

        String zUsed  = stats.getOrDefault("swap_used",  "--");
        String zTotal = stats.getOrDefault("swap_total", "--");
        zramText = "ZRAM " + zUsed + "/" + zTotal + "GB";

        String watt = stats.getOrDefault("bat_watt", "--");
        String bTemp = stats.getOrDefault("bat_temp", "--");
        pwrText = "PWR " + watt + "W (" + bTemp + "°C)";

        String dl = stats.getOrDefault("net_dl", "0 KB/s");
        String ul = stats.getOrDefault("net_ul", "0 KB/s");
        netText = "DL " + dl + " • UL " + ul;

        int[] dims = calcHudDimensions();
        int targetW = dims[0];
        int targetH = dims[1];

        if (params != null && (params.width != targetW || params.height != targetH)) {
            params.width  = targetW;
            params.height = targetH;
            hudView.requestLayout();
            try {
                windowManager.updateViewLayout(hudView, params);
            } catch (Exception ignored) {}
        }

        hudView.invalidate();
    }

    private static Map<String, String> readStats() {
        Map<String, String> map = new HashMap<>();
        File file = new File(statePath);
        if (!file.exists()) return map;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String content = sb.toString();

            // Pre-compiled STATS_PATTERN matcher for zero allocation
            Matcher m = STATS_PATTERN.matcher(content);
            while (m.find()) {
                String key = m.group(1);
                String val = m.group(2) != null ? m.group(2) : m.group(3);
                if (key != null && val != null) {
                    map.put(key, val);
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private static void readConfig() {
        File file = new File(configPath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String content = sb.toString();

            isVisible = parseBool(content, "visible", isVisible);
            showFps = parseBool(content, "show_fps", showFps);
            showCpu = parseBool(content, "show_cpu", showCpu);
            showGov = parseBool(content, "show_gov", showGov);
            showGpu = parseBool(content, "show_gpu", showGpu);
            showGpuGov = parseBool(content, "show_gpu_gov", showGpuGov);
            showRam = parseBool(content, "show_ram", showRam);
            showZram = parseBool(content, "show_zram", showZram);
            showBattery = parseBool(content, "show_battery", showBattery);
            showNet = parseBool(content, "show_net", showNet);
            isHorizontal = parseBool(content, "is_horizontal", isHorizontal);
            align = parseStr(content, "align", align);
            theme = parseStr(content, "theme", theme);
            customHexColor = parseStr(content, "custom_color", customHexColor);
            opacity = parseFloat(content, "opacity", opacity);
            scale = parseFloat(content, "scale", scale);
            fontSizeSp = parseInt(content, "font_size", fontSizeSp);
            cornerRadiusDp = parseInt(content, "corner_radius", cornerRadiusDp);
            bgWidthDp = parseInt(content, "bg_width", bgWidthDp);
            bgHeightDp = parseInt(content, "bg_height", bgHeightDp);
            refreshInterval = parseInt(content, "refresh_interval", refreshInterval);
            if (refreshInterval < 50) refreshInterval = 50;
        } catch (Exception ignored) {}
    }

    // Flexible & Robust JSON Key-Value Extractors
    private static String getJsonRawVal(String json, String key) {
        if (json == null || key == null) return null;
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([^,\\}\\]\\s]+))");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String val = m.group(1) != null ? m.group(1) : m.group(2);
            return val != null ? val.trim() : null;
        }
        return null;
    }

    private static boolean parseBool(String json, String key, boolean defVal) {
        String val = getJsonRawVal(json, key);
        if (val != null) {
            if ("true".equalsIgnoreCase(val) || "1".equals(val)) return true;
            if ("false".equalsIgnoreCase(val) || "0".equals(val)) return false;
        }
        return defVal;
    }

    private static String parseStr(String json, String key, String defVal) {
        String val = getJsonRawVal(json, key);
        if (val != null && !val.isEmpty()) {
            return val;
        }
        return defVal;
    }

    private static float parseFloat(String json, String key, float defVal) {
        String val = getJsonRawVal(json, key);
        if (val != null) {
            try {
                return Float.parseFloat(val);
            } catch (Exception ignored) {}
        }
        return defVal;
    }

    private static int parseInt(String json, String key, int defVal) {
        String val = getJsonRawVal(json, key);
        if (val != null) {
            try {
                return Math.round(Float.parseFloat(val));
            } catch (Exception ignored) {}
        }
        return defVal;
    }

    private static void readPosition() {
        File file = new File(posPath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String content = sb.toString();
            int newX = parseInt(content, "x", posX);
            int newY = parseInt(content, "y", posY);
            if (newY < 160) newY = 250;  // unified boundary
            posX = newX;
            posY = newY;
        } catch (Exception ignored) {}
    }

    private static void savePosition() {
        try (FileWriter fw = new FileWriter(posPath)) {
            fw.write("{\n  \"x\": " + posX + ",\n  \"y\": " + posY + "\n}\n");
        } catch (Exception ignored) {}
    }
}

