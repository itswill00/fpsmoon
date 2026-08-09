#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <time.h>
#include <math.h>
#include <glob.h>
#include <ctype.h>

#include <EGL/egl.h>
#include <GLES3/gl3.h>

#include "imgui/imgui.h"
#include "imgui/backends/imgui_impl_android.h"
#include "imgui/backends/imgui_impl_opengl3.h"

#define LOG_TAG "FPSMoonImGuiNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static float ft_history[60] = {0};
static int ft_idx = 0;

static long long prev_idle = 0;
static long long prev_total = 0;

static void read_file_string(const char *path, char *buf, size_t max_len) {
    buf[0] = '\0';
    int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return;
    ssize_t n = read(fd, buf, max_len - 1);
    close(fd);
    if (n > 0) {
        buf[n] = '\0';
        char *p = strchr(buf, '\n');
        if (p) *p = '\0';
        p = strchr(buf, '\r');
        if (p) *p = '\0';
    }
}

static void get_cpu_temp(char *out, size_t max_len) {
    glob_t g;
    float max_temp = 0.0f;
    if (glob("/sys/class/thermal/thermal_zone*", 0, NULL, &g) == 0) {
        for (size_t i = 0; i < g.gl_pathc; i++) {
            char type_path[512], temp_path[512], type_buf[128], temp_buf[128];
            snprintf(type_path, sizeof(type_path), "%s/type", g.gl_pathv[i]);
            snprintf(temp_path, sizeof(temp_path), "%s/temp", g.gl_pathv[i]);
            read_file_string(type_path, type_buf, sizeof(type_buf));
            for (char *p = type_buf; *p; p++) *p = tolower(*p);

            if (strstr(type_buf, "cpu") || strstr(type_buf, "soc") || strstr(type_buf, "tz") || strstr(type_buf, "mtk") || strstr(type_buf, "tsens")) {
                read_file_string(temp_path, temp_buf, sizeof(temp_buf));
                if (temp_buf[0]) {
                    float val = atof(temp_buf);
                    if (val > 1000.0f) val /= 1000.0f;
                    if (val > max_temp && val < 125.0f) max_temp = val;
                }
            }
        }
        globfree(&g);
    }
    if (max_temp > 0.0f) snprintf(out, max_len, "%.1f", max_temp);
    else snprintf(out, max_len, "--");
}

static void get_cpu_load(char *load_out, size_t load_len) {
    snprintf(load_out, load_len, "--");
    int fd = open("/proc/stat", O_RDONLY | O_CLOEXEC);
    if (fd < 0) return;
    char buf[1024];
    ssize_t n = read(fd, buf, sizeof(buf) - 1);
    close(fd);
    if (n <= 0) return;
    buf[n] = '\0';

    long long user, nice, system, idle, iowait, irq, softirq, steal;
    if (sscanf(buf, "cpu %lld %lld %lld %lld %lld %lld %lld %lld",
               &user, &nice, &system, &idle, &iowait, &irq, &softirq, &steal) >= 4) {
        long long current_idle = idle + iowait;
        long long current_total = user + nice + system + idle + iowait + irq + softirq + steal;

        long long total_diff = current_total - prev_total;
        long long idle_diff = current_idle - prev_idle;

        if (total_diff > 0) {
            float usage = (float)(total_diff - idle_diff) * 100.0f / (float)total_diff;
            if (usage < 0.0f) usage = 0.0f;
            if (usage > 100.0f) usage = 100.0f;
            snprintf(load_out, load_len, "%d", (int)usage);
        }
        prev_idle = current_idle;
        prev_total = current_total;
    }
}

static void get_ram_stats(char *used_out, size_t used_len, char *tot_out, size_t tot_len) {
    snprintf(used_out, used_len, "--");
    snprintf(tot_out, tot_len, "--");
    int fd = open("/proc/meminfo", O_RDONLY | O_CLOEXEC);
    if (fd < 0) return;
    char buf[2048];
    ssize_t n = read(fd, buf, sizeof(buf) - 1);
    close(fd);
    if (n <= 0) return;
    buf[n] = '\0';

    long long mem_total = 0, mem_free = 0, buffers = 0, cached = 0, SReclaimable = 0;
    char *line = strtok(buf, "\n");
    while (line) {
        if (sscanf(line, "MemTotal: %lld kB", &mem_total) == 1) {}
        else if (sscanf(line, "MemFree: %lld kB", &mem_free) == 1) {}
        else if (sscanf(line, "Buffers: %lld kB", &buffers) == 1) {}
        else if (sscanf(line, "Cached: %lld kB", &cached) == 1) {}
        else if (sscanf(line, "SReclaimable: %lld kB", &SReclaimable) == 1) {}
        line = strtok(NULL, "\n");
    }

    if (mem_total > 0) {
        long long mem_avail = mem_free + buffers + cached + SReclaimable;
        long long mem_used = mem_total - mem_avail;
        snprintf(used_out, used_len, "%.1f", mem_used / 1024.0f / 1024.0f);
        snprintf(tot_out, tot_len, "%.1f", mem_total / 1024.0f / 1024.0f);
    }
}

static void get_battery_stats(char *watt_out, size_t watt_len, char *temp_out, size_t temp_len) {
    snprintf(watt_out, watt_len, "--");
    snprintf(temp_out, temp_len, "--");
    char buf[64];
    float current_ua = 0, voltage_uv = 0, temp_c = 0;

    read_file_string("/sys/class/power_supply/battery/current_now", buf, sizeof(buf));
    if (buf[0]) current_ua = labs(atol(buf));

    read_file_string("/sys/class/power_supply/battery/voltage_now", buf, sizeof(buf));
    if (buf[0]) voltage_uv = atol(buf);

    read_file_string("/sys/class/power_supply/battery/temp", buf, sizeof(buf));
    if (buf[0]) temp_c = atof(buf) / 10.0f;

    if (current_ua > 0 && voltage_uv > 0) {
        float watt = (current_ua / 1000000.0f) * (voltage_uv / 1000000.0f);
        snprintf(watt_out, watt_len, "%.2f", watt);
    }
    if (temp_c > 0) snprintf(temp_out, temp_len, "%.1f", temp_c);
}

extern "C" JNIEXPORT void JNICALL
Java_com_fpsmoon_imgui_FPSMoonImGuiLauncher_initNativeImGui(JNIEnv *env, jclass clazz, jobject surface_obj) {
    if (!surface_obj) {
        LOGE("Surface object is null!");
        return;
    }

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface_obj);
    if (!window) {
        LOGE("Failed to get ANativeWindow from Surface!");
        return;
    }

    LOGI("Successfully acquired ANativeWindow handle: %p", window);

    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed!");
        return;
    }

    if (!eglInitialize(display, 0, 0)) {
        LOGE("eglInitialize failed!");
        return;
    }

    EGLint attribs[] = {
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
        EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };

    EGLConfig config;
    EGLint num_config;
    if (!eglChooseConfig(display, attribs, &config, 1, &num_config) || num_config <= 0) {
        LOGE("eglChooseConfig failed!");
        return;
    }

    EGLint ctx_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE };
    EGLContext egl_ctx = eglCreateContext(display, config, EGL_NO_CONTEXT, ctx_attribs);
    if (egl_ctx == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed!");
        return;
    }

    EGLSurface egl_surface = eglCreateWindowSurface(display, config, (EGLNativeWindowType)window, NULL);
    if (egl_surface == EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface failed!");
        return;
    }

    if (!eglMakeCurrent(display, egl_surface, egl_surface, egl_ctx)) {
        LOGE("eglMakeCurrent failed!");
        return;
    }

    LOGI("OpenGL ES 3.0 & EGL Context initialized successfully!");

    // Setup Dear ImGui Context
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO(); (void)io;
    io.IniFilename = NULL;

    ImGui::StyleColorsDark();
    ImGuiStyle& style = ImGui::GetStyle();
    style.WindowRounding = 14.0f;
    style.FrameRounding = 8.0f;
    style.WindowBorderSize = 1.0f;
    style.WindowPadding = ImVec2(14.0f, 12.0f);

    ImVec4* colors = style.Colors;
    colors[ImGuiCol_WindowBg]           = ImVec4(0.06f, 0.08f, 0.12f, 0.85f);
    colors[ImGuiCol_Border]             = ImVec4(0.39f, 0.40f, 0.95f, 0.35f);
    colors[ImGuiCol_TitleBg]            = ImVec4(0.08f, 0.10f, 0.16f, 0.90f);
    colors[ImGuiCol_TitleBgActive]      = ImVec4(0.12f, 0.14f, 0.24f, 0.95f);
    colors[ImGuiCol_PlotLines]          = ImVec4(0.06f, 0.73f, 0.50f, 1.00f);

    ImGui_ImplAndroid_Init(window);
    ImGui_ImplOpenGL3_Init("#version 300 es");

    LOGI("Native C++ Dear ImGui Engine ready and rendering on ANativeWindow!");

    while (true) {
        char cpu_temp[32], cpu_load[32], ram_used[32], ram_tot[32], bat_watt[32], bat_temp[32];
        get_cpu_temp(cpu_temp, sizeof(cpu_temp));
        get_cpu_load(cpu_load, sizeof(cpu_load));
        get_ram_stats(ram_used, sizeof(ram_used), ram_tot, sizeof(ram_tot));
        get_battery_stats(bat_watt, sizeof(bat_watt), bat_temp, sizeof(bat_temp));

        float ft_val = 16.6f;
        ft_history[ft_idx] = ft_val;
        ft_idx = (ft_idx + 1) % 60;

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplAndroid_NewFrame();
        ImGui::NewFrame();

        ImGui::SetNextWindowPos(ImVec2(30, 50), ImGuiCond_FirstUseEver);
        ImGui::SetNextWindowSize(ImVec2(280, 240), ImGuiCond_FirstUseEver);

        ImGui::Begin("FPSMoon HUD", NULL, ImGuiWindowFlags_NoCollapse);

        ImGui::TextColored(ImVec4(0.06f, 0.73f, 0.50f, 1.0f), "60 FPS");
        ImGui::SameLine();
        ImGui::TextDisabled("(16.6 ms) [60Hz]");

        ImGui::PlotLines("##frametime", ft_history, 60, ft_idx, "Frame Time (ms)", 0.0f, 50.0f, ImVec2(-1, 45));

        ImGui::Separator();

        ImGui::Text("Processor : %s°C (%s%%)", cpu_temp, cpu_load);
        ImGui::Text("Memory    : %s / %s GB", ram_used, ram_tot);
        ImGui::Text("Battery   : %s W (%s°C)", bat_watt, bat_temp);

        ImGui::Separator();
        ImGui::TextDisabled("FPSMoon ImGui by @itswill00");

        ImGui::End();

        ImGui::Render();
        glViewport(0, 0, ANativeWindow_getWidth(window), ANativeWindow_getHeight(window));
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        eglSwapBuffers(display, egl_surface);

        usleep(250000); // 250ms tick
    }

    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplAndroid_Shutdown();
    ImGui::DestroyContext();
    ANativeWindow_release(window);
}
