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

public class TestTypeface {
    public static void main(String[] args) {
        System.out.println("[Test] Starting Window Test...");
        try {
            Looper.prepareMainLooper();
            
            try {
                Method m = Typeface.class.getDeclaredMethod("loadPreinstalledSystemFontMap");
                m.setAccessible(true);
                m.invoke(null);
                System.out.println("[Test] loadPreinstalledSystemFontMap success!");
            } catch (Exception e) {
                System.out.println("[Test] Font map init fallback: " + e);
            }

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getMethod("systemMain");
            Object activityThread = systemMainMethod.invoke(null);
            Context context = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            TextView tv = new TextView(context);
            tv.setText(" FPS MOON ACTIVE ");
            tv.setTextSize(18);
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.BLACK);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    2038, // TYPE_APPLICATION_OVERLAY
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 50;
            params.y = 150;

            try {
                wm.addView(tv, params);
                System.out.println("[Test] Window added successfully with TYPE_APPLICATION_OVERLAY (2038)");
            } catch (Exception e1) {
                System.err.println("[Test] Failed 2038: " + e1.getMessage());
                try {
                    params.type = 2034; // TYPE_ACCESSIBILITY_OVERLAY
                    wm.addView(tv, params);
                    System.out.println("[Test] Window added successfully with TYPE_ACCESSIBILITY_OVERLAY (2034)");
                } catch (Exception e2) {
                    System.err.println("[Test] Failed 2034: " + e2.getMessage());
                    params.type = 2003; // TYPE_SYSTEM_ALERT
                    wm.addView(tv, params);
                    System.out.println("[Test] Window added successfully with TYPE_SYSTEM_ALERT (2003)");
                }
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                System.out.println("[Test] Window is running on screen.");
            }, 1000);

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
