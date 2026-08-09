package com.fpsmoon.imgui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.hardware.display.DisplayManager;

public class FPSMoonImGuiLauncher {
    static {
        try {
            System.loadLibrary("fpsmoon_imgui");
        } catch (Throwable t) {
            try {
                System.load("/data/adb/modules/fps_moon/bin/libfpsmoon_imgui.so");
            } catch (Throwable t2) {
                System.err.println("[FPSMoon ImGui] Failed to load libfpsmoon_imgui.so: " + t2.getMessage());
            }
        }
    }

    public static native void initNativeImGui(Surface surface);

    public static void main(String[] args) {
        System.out.println("[FPSMoon ImGui] Starting Native C++ Dear ImGui Overlay Engine...");

        try {
            Looper.prepareMainLooper();

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("systemMain").invoke(null);
            Context sysContext = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);

            if (sysContext == null) {
                System.err.println("[FPSMoon ImGui Error] Unable to acquire system context.");
                return;
            }

            Context context = sysContext;
            try {
                DisplayManager dm = (DisplayManager) sysContext.getSystemService(Context.DISPLAY_SERVICE);
                if (dm != null) {
                    Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
                    if (defaultDisplay != null) {
                        Context displayCtx = sysContext.createDisplayContext(defaultDisplay);
                        if (displayCtx != null) context = displayCtx;
                    }
                }
            } catch (Throwable ignored) {}

            final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                System.err.println("[FPSMoon ImGui Error] Unable to acquire WindowManager.");
                return;
            }

            final SurfaceView surfaceView = new SurfaceView(context);
            surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                2038, // TYPE_APPLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            );

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(final SurfaceHolder holder) {
                    System.out.println("[FPSMoon ImGui] Surface created! Passing ANativeWindow to C++ ImGui...");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            initNativeImGui(holder.getSurface());
                        }
                    }).start();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {}
            });

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        windowManager.addView(surfaceView, params);
                        System.out.println("[FPSMoon ImGui] SurfaceView added to WindowManager layer 2038!");
                    } catch (Throwable t) {
                        System.err.println("[FPSMoon ImGui Error] addView failed: " + t.getMessage());
                    }
                }
            });

            Looper.loop();

        } catch (Throwable t) {
            System.err.println("[FPSMoon ImGui Fatal] " + t.getMessage());
            t.printStackTrace();
        }
    }
}
