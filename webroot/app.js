// FPS Moon Settings Controller - Flawless Non-Overlapping Sync Edition

let stateConfig = {
    visible: true,
    show_fps: true,
    show_cpu: true,
    show_gov: false,
    show_gpu: true,
    show_gpu_gov: false,
    show_ram: false,
    show_zram: false,
    show_battery: true,
    show_net: false,
    is_horizontal: true,
    align: "left",
    theme: "cyber_neon",
    custom_color: "#6366F1",
    opacity: 0.85,
    scale: 1.0,
    font_size: 12,
    corner_radius: 14,
    bg_width: 250,
    bg_height: 56,
    refresh_interval: 250,
    target_fps: 60
};

let positionState = { x: 60, y: 250 };
let cbSeq = 0;
let toastTimer = null;
let saveDebounceTimer = null;
let isFetchingStats = false;

document.addEventListener("DOMContentLoaded", () => {
    initEvents();
    loadConfig();
    loadPosition();
    scheduleStatsLoop();
});

function showToast(msg) {
    const pill = document.getElementById("toastPill");
    const label = document.getElementById("toastMsg");
    if (!pill || !label) return;

    label.innerText = msg;
    pill.classList.add("show");

    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        pill.classList.remove("show");
    }, 1800);
}

function isKsuAvailable() {
    return typeof ksu !== "undefined" && typeof ksu.exec === "function";
}

function execCmd(cmd) {
    return new Promise((resolve) => {
        if (!isKsuAvailable()) {
            resolve('');
            return;
        }
        const id = `_fpsm_${++cbSeq}_${Date.now()}`;
        const timer = setTimeout(() => {
            if (window[id]) {
                delete window[id];
                resolve('');
            }
        }, 5000);

        window[id] = (errno, stdout, stderr) => {
            clearTimeout(timer);
            delete window[id];
            resolve(stdout || stderr || '');
        };

        try {
            ksu.exec(cmd, '{}', id);
        } catch (e) {
            clearTimeout(timer);
            delete window[id];
            resolve('');
        }
    });
}

async function loadConfig() {
    try {
        const out = await execCmd('cat /data/adb/modules/fps_moon/state/config.json 2>/dev/null');
        if (out && out.trim().startsWith('{')) {
            const data = JSON.parse(out.trim());
            stateConfig = { ...stateConfig, ...data };
        }
    } catch (e) {}
    syncUiFromState();
}

async function loadPosition() {
    try {
        const out = await execCmd('cat /data/adb/modules/fps_moon/state/position.json 2>/dev/null');
        if (out && out.trim().startsWith('{')) {
            const data = JSON.parse(out.trim());
            if (data.x !== undefined && data.y !== undefined) {
                positionState = data;
                if (positionState.y < 180) positionState.y = 250;
            }
        }
    } catch (e) {}
}

async function saveConfig(silent = false) {
    syncStateFromUi();
    const configStr = JSON.stringify(stateConfig);
    const escaped = configStr.replace(/'/g, "'\\''");
    await execCmd(`mkdir -p /data/adb/modules/fps_moon/state && echo '${escaped}' > /data/adb/modules/fps_moon/state/config.json`);
    if (!silent) showToast("Settings saved");
}

function triggerInstantSave(silent = false) {
    syncStateFromUi();
    if (saveDebounceTimer) clearTimeout(saveDebounceTimer);
    saveDebounceTimer = setTimeout(() => {
        saveConfig(silent);
    }, 40);
}

async function savePosition(silent = false) {
    if (positionState.y < 180) positionState.y = 250;
    const posStr = JSON.stringify(positionState);
    const escaped = posStr.replace(/'/g, "'\\''");
    await execCmd(`mkdir -p /data/adb/modules/fps_moon/state && echo '${escaped}' > /data/adb/modules/fps_moon/state/position.json`);
    if (!silent) showToast("Position saved");
}

function setOrientation(isHorizontal) {
    stateConfig.is_horizontal = isHorizontal;
    const btnH = document.getElementById("btnOrientHoriz");
    const btnV = document.getElementById("btnOrientVert");

    if (btnH && btnV) {
        if (isHorizontal) {
            btnH.classList.add("active");
            btnV.classList.remove("active");
        } else {
            btnV.classList.add("active");
            btnH.classList.remove("active");
        }
    }
    showToast(isHorizontal ? "Horizontal pill layout" : "Vertical stack layout");
    saveConfig(true);
}

function setAlignment(alignType) {
    stateConfig.align = alignType;
    const btnL = document.getElementById("btnAlignLeft");
    const btnC = document.getElementById("btnAlignCenter");
    const btnR = document.getElementById("btnAlignRight");

    if (btnL) btnL.classList.remove("active");
    if (btnC) btnC.classList.remove("active");
    if (btnR) btnR.classList.remove("active");

    if (alignType === 'center' && btnC) btnC.classList.add("active");
    else if (alignType === 'right' && btnR) btnR.classList.add("active");
    else if (btnL) btnL.classList.add("active");

    showToast(`Alignment set to ${alignType}`);
    saveConfig(true);
}

function syncUiFromState() {
    const masterEl = document.getElementById("masterToggle");
    if (masterEl) masterEl.checked = stateConfig.visible;

    const bannerStatus = document.getElementById("bannerStatus");
    if (bannerStatus) {
        bannerStatus.innerText = stateConfig.visible ? "Active" : "Disabled";
        bannerStatus.className = stateConfig.visible ? "badge-pill green" : "badge-pill purple";
    }

    const btnH = document.getElementById("btnOrientHoriz");
    const btnV = document.getElementById("btnOrientVert");
    if (btnH && btnV) {
        if (stateConfig.is_horizontal !== false) {
            btnH.classList.add("active");
            btnV.classList.remove("active");
        } else {
            btnV.classList.add("active");
            btnH.classList.remove("active");
        }
    }

    const btnL = document.getElementById("btnAlignLeft");
    const btnC = document.getElementById("btnAlignCenter");
    const btnR = document.getElementById("btnAlignRight");
    if (btnL && btnC && btnR) {
        btnL.classList.remove("active");
        btnC.classList.remove("active");
        btnR.classList.remove("active");

        if (stateConfig.align === 'center') btnC.classList.add("active");
        else if (stateConfig.align === 'right') btnR.classList.add("active");
        else btnL.classList.add("active");
    }

    document.getElementById("chkFps").checked = stateConfig.show_fps;
    document.getElementById("chkCpu").checked = stateConfig.show_cpu;
    if (document.getElementById("chkGov")) document.getElementById("chkGov").checked = stateConfig.show_gov;
    document.getElementById("chkGpu").checked = stateConfig.show_gpu;
    if (document.getElementById("chkGpuGov")) document.getElementById("chkGpuGov").checked = stateConfig.show_gpu_gov;
    document.getElementById("chkRam").checked = stateConfig.show_ram;
    document.getElementById("chkBattery").checked = stateConfig.show_battery;
    if (document.getElementById("chkNet")) document.getElementById("chkNet").checked = stateConfig.show_net;

    if (document.getElementById("rngBgWidth")) {
        document.getElementById("rngBgWidth").value = stateConfig.bg_width || 250;
        document.getElementById("bgWidthVal").innerText = (stateConfig.bg_width || 250) + "px";
    }
    if (document.getElementById("rngBgHeight")) {
        document.getElementById("rngBgHeight").value = stateConfig.bg_height || 56;
        document.getElementById("bgHeightVal").innerText = (stateConfig.bg_height || 56) + "px";
    }

    document.getElementById("rngScale").value = Math.round((stateConfig.scale || 1.0) * 100);
    document.getElementById("scaleVal").innerText = (stateConfig.scale || 1.0).toFixed(1) + "x";

    document.getElementById("rngFont").value = stateConfig.font_size || 12;
    document.getElementById("fontVal").innerText = (stateConfig.font_size || 12) + "sp";

    document.getElementById("rngRadius").value = stateConfig.corner_radius || 14;
    document.getElementById("radiusVal").innerText = (stateConfig.corner_radius || 14) + "px";

    if (document.getElementById("rngInterval")) {
        document.getElementById("rngInterval").value = stateConfig.refresh_interval || 250;
        document.getElementById("intervalVal").innerText = (stateConfig.refresh_interval || 250) + "ms";
    }

    document.getElementById("rngOpacity").value = Math.round((stateConfig.opacity || 0.85) * 100);
    document.getElementById("opacityVal").innerText = Math.round((stateConfig.opacity || 0.85) * 100) + "%";
}

function syncStateFromUi() {
    stateConfig.visible = document.getElementById("masterToggle").checked;
    stateConfig.show_fps = document.getElementById("chkFps").checked;
    stateConfig.show_cpu = document.getElementById("chkCpu").checked;
    if (document.getElementById("chkGov")) stateConfig.show_gov = document.getElementById("chkGov").checked;
    stateConfig.show_gpu = document.getElementById("chkGpu").checked;
    if (document.getElementById("chkGpuGov")) stateConfig.show_gpu_gov = document.getElementById("chkGpuGov").checked;
    stateConfig.show_ram = document.getElementById("chkRam").checked;
    stateConfig.show_battery = document.getElementById("chkBattery").checked;
    if (document.getElementById("chkNet")) stateConfig.show_net = document.getElementById("chkNet").checked;

    if (document.getElementById("rngBgWidth")) stateConfig.bg_width = parseInt(document.getElementById("rngBgWidth").value);
    if (document.getElementById("rngBgHeight")) stateConfig.bg_height = parseInt(document.getElementById("rngBgHeight").value);

    stateConfig.scale = parseInt(document.getElementById("rngScale").value) / 100.0;
    stateConfig.font_size = parseInt(document.getElementById("rngFont").value);
    stateConfig.corner_radius = parseInt(document.getElementById("rngRadius").value);
    if (document.getElementById("rngInterval")) stateConfig.refresh_interval = parseInt(document.getElementById("rngInterval").value);
    stateConfig.opacity = parseInt(document.getElementById("rngOpacity").value) / 100.0;
}

function toggleRowSwitch(id) {
    const el = document.getElementById(id);
    if (el) {
        el.checked = !el.checked;
        saveConfig();
    }
}

function toggleOverlayQuick() {
    const el = document.getElementById("masterToggle");
    if (el) {
        el.checked = !el.checked;
        saveConfig();
    }
}

function resetPositionQuick() {
    positionState.x = 60;
    positionState.y = 250;
    savePosition();
}

function initEvents() {
    const toggles = ["masterToggle", "chkFps", "chkCpu", "chkGov", "chkGpu", "chkGpuGov", "chkRam", "chkBattery", "chkNet"];
    toggles.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("change", () => saveConfig());
    });

    const ranges = [
        { id: "rngBgWidth", label: "bgWidthVal", unit: "px", calc: val => val },
        { id: "rngBgHeight", label: "bgHeightVal", unit: "px", calc: val => val },
        { id: "rngScale", label: "scaleVal", unit: "x", calc: val => (val / 100.0).toFixed(1) },
        { id: "rngFont", label: "fontVal", unit: "sp", calc: val => val },
        { id: "rngRadius", label: "radiusVal", unit: "px", calc: val => val },
        { id: "rngInterval", label: "intervalVal", unit: "ms", calc: val => val },
        { id: "rngOpacity", label: "opacityVal", unit: "%", calc: val => val }
    ];

    ranges.forEach(r => {
        const el = document.getElementById(r.id);
        if (el) {
            el.addEventListener("input", (e) => {
                document.getElementById(r.label).innerText = r.calc(e.target.value) + r.unit;
                triggerInstantSave(true);
            });
            el.addEventListener("change", () => saveConfig());
        }
    });
}

function applyPreset(type) {
    const btnC = document.getElementById("presetCompact");
    const btnM = document.getElementById("presetMinimal");
    const btnD = document.getElementById("presetDetailed");

    if (btnC) btnC.classList.remove("active");
    if (btnM) btnM.classList.remove("active");
    if (btnD) btnD.classList.remove("active");

    if (type === 'gaming') {
        if (btnC) btnC.classList.add("active");
        stateConfig.show_fps = true;
        stateConfig.show_cpu = true;
        stateConfig.show_gov = false;
        stateConfig.show_gpu = true;
        stateConfig.show_gpu_gov = false;
        stateConfig.show_ram = false;
        stateConfig.show_battery = true;
        stateConfig.show_net = false;
        stateConfig.bg_width = 250;
        stateConfig.bg_height = 56;
        showToast("Applied Compact Preset");
    } else if (type === 'minimal') {
        if (btnM) btnM.classList.add("active");
        stateConfig.show_fps = true;
        stateConfig.show_cpu = false;
        stateConfig.show_gov = false;
        stateConfig.show_gpu = false;
        stateConfig.show_gpu_gov = false;
        stateConfig.show_ram = false;
        stateConfig.show_battery = false;
        stateConfig.show_net = false;
        stateConfig.bg_width = 180;
        stateConfig.bg_height = 42;
        showToast("Applied Minimal Preset");
    } else if (type === 'full') {
        if (btnD) btnD.classList.add("active");
        stateConfig.show_fps = true;
        stateConfig.show_cpu = true;
        stateConfig.show_gov = true;
        stateConfig.show_gpu = true;
        stateConfig.show_gpu_gov = true;
        stateConfig.show_ram = true;
        stateConfig.show_battery = true;
        stateConfig.show_net = true;
        stateConfig.bg_width = 320;
        stateConfig.bg_height = 68;
        showToast("Applied Detailed Preset");
    }
    syncUiFromState();
    saveConfig(true);
}

function scheduleStatsLoop() {
    async function fetchTick() {
        if (isFetchingStats) return;
        isFetchingStats = true;
        try {
            const out = await execCmd('cat /data/adb/modules/fps_moon/state/stats.json 2>/dev/null');
            if (out && out.trim().startsWith('{')) {
                const stats = JSON.parse(out.trim());
                updateDashboardStats(stats);
            }
        } catch (e) {
        } finally {
            isFetchingStats = false;
            setTimeout(fetchTick, 600);
        }
    }
    fetchTick();
}

function updateDashboardStats(stats) {
    if (stats.fps) {
        document.getElementById("dashFps").innerText = `${stats.fps} FPS`;
        if (document.getElementById("chipFps")) document.getElementById("chipFps").innerText = stats.fps;
    }
    if (stats.frametime) {
        document.getElementById("dashFt").innerText = `${stats.frametime} ms`;
    }
    if (stats.cpu_load) {
        document.getElementById("dashCpu").innerText = `${stats.cpu_load}%`;
        document.getElementById("dashCpuSub").innerText = `${stats.cpu_freq || '--'} | ${stats.cpu_temp || '--'}°C`;
        if (document.getElementById("chipCpu")) document.getElementById("chipCpu").innerText = `${stats.cpu_load}%`;
    }
    if (stats.gpu_load) {
        document.getElementById("dashGpu").innerText = `${stats.gpu_load}%`;
        document.getElementById("dashGpuSub").innerText = `${stats.gpu_freq || '--'} | ${stats.gpu_temp || '--'}°C`;
        if (document.getElementById("chipGpu")) document.getElementById("chipGpu").innerText = `${stats.gpu_load}%`;
    }
    if (stats.bat_watt) {
        document.getElementById("dashBat").innerText = `${stats.bat_watt} W`;
        document.getElementById("dashBatSub").innerText = `Battery: ${stats.bat_temp || '--'}°C`;
        if (document.getElementById("chipBat")) document.getElementById("chipBat").innerText = `${stats.bat_watt}W`;
    }
}
