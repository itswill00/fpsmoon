#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <time.h>
#include <math.h>
#include <EGL/egl.h>
#include <GLES3/gl3.h>

#include "imgui/imgui.h"
#include "imgui/backends/imgui_impl_opengl3.h"

// FPSMoon Native ImGui Overlay Engine
// Developed & Maintained by @itswill00

static char state_dir[256] = "/data/adb/modules/fps_moon/state";
static char stats_file[512] = "/data/adb/modules/fps_moon/state/stats.json";

// Frame Time History Buffer for ImGui Plot
static float ft_history[60] = {0};
static int ft_idx = 0;

static void parse_json_val(const char *json, const char *key, char *out, size_t max_len, const char *def_val) {
    snprintf(out, max_len, "%s", def_val);
    if (!json || !key) return;
    char search_key[128];
    snprintf(search_key, sizeof(search_key), "\"%s\"", key);
    const char *p = strstr(json, search_key);
    if (p) {
        const char *colon = strchr(p, ':');
        if (colon) {
            colon++;
            while (*colon == ' ' || *colon == '\t' || *colon == '\"') colon++;
            size_t idx = 0;
            while (*colon && *colon != '\"' && *colon != ',' && *colon != '}' && *colon != '\n' && *colon != '\r' && idx < max_len - 1) {
                out[idx++] = *colon++;
            }
            out[idx] = '\0';
        }
    }
}

int main(int argc, char **argv) {
    char *env_state = getenv("FPSMOON_STATE_DIR");
    if (env_state && strlen(env_state) > 0) {
        snprintf(state_dir, sizeof(state_dir), "%s", env_state);
    } else if (argc > 1 && argv[1] != NULL && strlen(argv[1]) > 0) {
        snprintf(state_dir, sizeof(state_dir), "%s", argv[1]);
    }
    snprintf(stats_file, sizeof(stats_file), "%s/stats.json", state_dir);

    printf("[FPSMoon ImGui] Initializing Native C++ Dear ImGui Engine on %s...\n", state_dir);

    // Initialize ImGui Context
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO(); (void)io;
    io.IniFilename = NULL; // Disable imgui.ini disk writes

    // Setup Clean Natural Dark Glassmorphism Theme (Aligned with FPSMoon Design System)
    ImGui::StyleColorsDark();
    ImGuiStyle& style = ImGui::GetStyle();
    style.WindowRounding = 14.0f;
    style.FrameRounding = 8.0f;
    style.PopupRounding = 8.0f;
    style.ScrollbarRounding = 8.0f;
    style.GrabRounding = 6.0f;
    style.WindowBorderSize = 1.0f;
    style.WindowPadding = ImVec2(14.0f, 12.0f);
    style.ItemSpacing = ImVec2(8.0f, 6.0f);

    // Natural Soft Palette (Indigo & Soft Mint Accent)
    ImVec4* colors = style.Colors;
    colors[ImGuiCol_WindowBg]           = ImVec4(0.06f, 0.08f, 0.12f, 0.85f); // Soft dark slate background
    colors[ImGuiCol_Border]             = ImVec4(0.39f, 0.40f, 0.95f, 0.35f); // Subtle soft indigo border
    colors[ImGuiCol_TitleBg]            = ImVec4(0.08f, 0.10f, 0.16f, 0.90f);
    colors[ImGuiCol_TitleBgActive]      = ImVec4(0.12f, 0.14f, 0.24f, 0.95f);
    colors[ImGuiCol_Button]             = ImVec4(0.39f, 0.40f, 0.95f, 0.50f);
    colors[ImGuiCol_ButtonHovered]      = ImVec4(0.49f, 0.50f, 1.00f, 0.75f);
    colors[ImGuiCol_PlotLines]          = ImVec4(0.06f, 0.73f, 0.50f, 1.00f); // Soft natural mint green
    colors[ImGuiCol_PlotLinesHovered]   = ImVec4(0.10f, 0.85f, 0.60f, 1.00f);

    // Initialize OpenGL ES 3.0 Backend
    ImGui_ImplOpenGL3_Init("#version 300 es");

    printf("[FPSMoon ImGui] ImGui Native Engine ready.\n");

    // ImGui Render Loop
    bool running = true;
    while (running) {
        // Read latest stats.json
        char json_buf[4096] = {0};
        int fd = open(stats_file, O_RDONLY);
        if (fd >= 0) {
            ssize_t n = read(fd, json_buf, sizeof(json_buf) - 1);
            close(fd);
            if (n > 0) json_buf[n] = '\0';
        }

        char fps_str[32], ft_str[32], hz_str[32], cpu_temp[32], cpu_load[32], gpu_load[32], gpu_temp[32], ram_used[32], ram_tot[32], bat_watt[32], bat_temp[32];
        parse_json_val(json_buf, "fps", fps_str, sizeof(fps_str), "60");
        parse_json_val(json_buf, "frametime", ft_str, sizeof(ft_str), "16.6");
        parse_json_val(json_buf, "screen_hz", hz_str, sizeof(hz_str), "60Hz");
        parse_json_val(json_buf, "cpu_temp", cpu_temp, sizeof(cpu_temp), "--");
        parse_json_val(json_buf, "cpu_load", cpu_load, sizeof(cpu_load), "--");
        parse_json_val(json_buf, "gpu_load", gpu_load, sizeof(gpu_load), "--");
        parse_json_val(json_buf, "gpu_temp", gpu_temp, sizeof(gpu_temp), "--");
        parse_json_val(json_buf, "ram_used", ram_used, sizeof(ram_used), "--");
        parse_json_val(json_buf, "ram_total", ram_tot, sizeof(ram_tot), "--");
        parse_json_val(json_buf, "bat_watt", bat_watt, sizeof(bat_watt), "--");
        parse_json_val(json_buf, "bat_temp", bat_temp, sizeof(bat_temp), "--");

        float ft_val = atof(ft_str);
        if (ft_val <= 0.0f) ft_val = 16.6f;
        ft_history[ft_idx] = ft_val;
        ft_idx = (ft_idx + 1) % 60;

        ImGui_ImplOpenGL3_NewFrame();
        ImGui::NewFrame();

        // Render FPSMoon ImGui Floating Overlay Window
        ImGui::SetNextWindowPos(ImVec2(50, 80), ImGuiCond_FirstUseEver);
        ImGui::SetNextWindowSize(ImVec2(280, 240), ImGuiCond_FirstUseEver);

        ImGui::Begin("FPSMoon HUD", NULL, ImGuiWindowFlags_NoCollapse);

        // Natural Header: FPS & Refresh Rate
        ImGui::TextColored(ImVec4(0.06f, 0.73f, 0.50f, 1.0f), "%s FPS", fps_str);
        ImGui::SameLine();
        ImGui::TextDisabled("(%s ms) [%s]", ft_str, hz_str);

        // Smooth Real-Time Frametime Graph
        ImGui::PlotLines("##frametime", ft_history, 60, ft_idx, "Frame Time (ms)", 0.0f, 50.0f, ImVec2(-1, 45));

        ImGui::Separator();

        // Clean Hardware Metrics
        ImGui::Text("Processor : %s°C (%s%%)", cpu_temp, cpu_load);
        ImGui::Text("Graphics  : %s°C (%s%%)", gpu_temp, gpu_load);
        ImGui::Text("Memory    : %s / %s GB", ram_used, ram_tot);
        ImGui::Text("Battery   : %s W (%s°C)", bat_watt, bat_temp);

        ImGui::Separator();
        ImGui::TextDisabled("FPSMoon by @itswill00");

        ImGui::End();

        ImGui::Render();
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

        usleep(250000); // 250ms tick
    }

    ImGui_ImplOpenGL3_Shutdown();
    ImGui::DestroyContext();
    return 0;
}
