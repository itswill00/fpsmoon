package com.fpsmoon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import java.lang.reflect.Method;

public class TestRedmiWindow {
    public static void main(String[] args) {
        System.out.println("[TestRedmi] Starting Window Test on Redmi HyperOS...");
        try {
            Looper.prepareMainLooper();
            
            try {
                Method m = Typeface.class.getDeclaredMethod("loadPreinstalledSystemFontMap");
                m.setAccessible(true);
                m.invoke(null);
            } catch (Throwable ignored) {}

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getMethod("systemMain");
            Object activityThread = systemMainMethod.invoke(null);
            Context context = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            TextView tv = new TextView(context);
            tv.setText(" FPS MOON TEST OVERLAY - 60 FPS ");
            tv.setTextSize(22);
            tv.setTextColor(Color.YELLOW);
            tv.setBackgroundColor(Color.parseColor("#EE111122"));
            tv.setPadding(30, 20, 30, 20);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    2038, // TYPE_APPLICATION_OVERLAY
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 100;
            params.y = 300; // Well below status bar (y=300)

            try {
                wm.addView(tv, params);
                System.out.println("[TestRedmi] Window added at (100, 300) with TYPE_APPLICATION_OVERLAY (2038)");
            } catch (Exception e1) {
                System.err.println("[TestRedmi] Failed 2038: " + e1.getMessage());
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                System.out.println("[TestRedmi] Window is active at (100, 300)");
            }, 1000);

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
