package com.fpsmoon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import java.lang.reflect.Method;

public class DebugRedmi {
    public static void main(String[] args) {
        System.out.println("=== HYPEROS OVERLAY DIAGNOSTIC ===");
        try {
            Looper.prepareMainLooper();
            
            try {
                Method m = Typeface.class.getDeclaredMethod("loadPreinstalledSystemFontMap");
                m.setAccessible(true);
                m.invoke(null);
                System.out.println("[+] Typeface font map initialized.");
            } catch (Throwable t) {
                System.out.println("[-] Typeface init error: " + t.getMessage());
            }

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method systemMainMethod = activityThreadClass.getMethod("systemMain");
            Object activityThread = systemMainMethod.invoke(null);
            Context sysContext = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);
            System.out.println("[+] System Context: " + sysContext);

            DisplayManager dm = (DisplayManager) sysContext.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
            System.out.println("[+] Default Display: " + defaultDisplay);

            Context displayContext = sysContext.createDisplayContext(defaultDisplay);
            System.out.println("[+] Display Context created: " + displayContext);

            WindowManager wm = (WindowManager) displayContext.getSystemService(Context.WINDOW_SERVICE);
            System.out.println("[+] WindowManager acquired: " + wm);

            TextView tv = new TextView(displayContext);
            tv.setText(" *** FPS MOON REDMI OVERLAY ACTIVE *** ");
            tv.setTextSize(24);
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.GREEN);
            tv.setPadding(40, 40, 40, 40);

            int[] types = new int[]{ 2038, 2034, 2015, 2006, 2003, 2002 };
            for (int type : types) {
                try {
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            type,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                            PixelFormat.TRANSLUCENT
                    );
                    params.gravity = Gravity.CENTER;
                    params.setTitle("FPSMoonOverlay");

                    wm.addView(tv, params);
                    System.out.println("[SUCCESS] Added view with Window Type " + type);
                    System.out.println("[+] View isAttached: " + tv.isAttachedToWindow());
                    System.out.println("[+] View Visibility: " + tv.getVisibility());
                    break;
                } catch (Exception e) {
                    System.err.println("[-] Type " + type + " failed: " + e.getMessage());
                }
            }

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
