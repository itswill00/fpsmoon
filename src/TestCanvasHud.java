package com.fpsmoon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.Looper;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import java.lang.reflect.Method;

public class TestCanvasHud {
    static class CustomHudView extends View {
        private Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintTextFps = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint paintTextSub = new Paint(Paint.ANTI_ALIAS_FLAG);
        private RectF rectBg = new RectF();

        public CustomHudView(Context context) {
            super(context);
            paintBg.setColor(Color.parseColor("#EE0B0D10"));
            paintBg.setStyle(Paint.Style.FILL);

            paintTextFps.setColor(Color.parseColor("#10B981"));
            paintTextFps.setTextSize(48);
            paintTextFps.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

            paintTextSub.setColor(Color.parseColor("#94A3B8"));
            paintTextSub.setTextSize(28);
            paintTextSub.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(480, 140);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            rectBg.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rectBg, 28, 28, paintBg);

            canvas.drawText("60 FPS", 24, 56, paintTextFps);
            canvas.drawText("CPU 45°C • GPU 72% • 3.4W", 24, 106, paintTextSub);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TESTING CUSTOM CANVAS HUD ON REDMI ===");
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
            Context sysContext = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);

            DisplayManager dm = (DisplayManager) sysContext.getSystemService(Context.DISPLAY_SERVICE);
            Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
            Context displayContext = sysContext.createDisplayContext(defaultDisplay);

            WindowManager wm = (WindowManager) displayContext.getSystemService(Context.WINDOW_SERVICE);

            CustomHudView hudView = new CustomHudView(displayContext);

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
            params.x = 80;
            params.y = 250;

            wm.addView(hudView, params);
            System.out.println("[SUCCESS] Custom Canvas View added to WindowManager cleanly!");

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
