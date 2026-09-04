import { defineStore } from 'pinia'
import { ref } from 'vue'
import { execCommand, readStateFile, writeStateFile } from '@/helpers/shell'

export const useFpsMoonStore = defineStore('fpsmoon', () => {
  const moduleVersion = ref('v1.0.1')
  
  const config = ref({
    visible: true,
    show_fps: true,
    show_cpu: true,
    show_cpu_freq: true,
    show_gov: true,
    show_gpu: true,
    show_gpu_freq: true,
    show_gpu_gov: true,
    show_ram: true,
    show_zram: false,
    show_battery: true,
    show_net: false,
    is_horizontal: false,
    align: 'left',
    theme: 'cyber_neon',
    custom_color: '#6366F1',
    opacity: 0.58,
    scale: 0.79,
    font_size: 11,
    corner_radius: 14,
    bg_width: 150,
    bg_height: 160,
    refresh_interval: 850,
    target_fps: 60
  })

  const position = ref({ x: 565, y: 156 })

  const stats = ref({
    fps: '--',
    frametime: '--',
    cpu_load: '--',
    cpu_freq: '--',
    cpu_temp: '--',
    cpu_gov: '--',
    cpu_policy: '--',
    gpu_load: '--',
    gpu_freq: '--',
    gpu_temp: '--',
    gpu_gov: '--',
    gpu_policy: '--',
    ram_used: '--',
    ram_total: '--',
    swap_used: '--',
    swap_total: '--',
    bat_watt: '--',
    bat_temp: '--',
    bat_curr: '--',
    bat_volt: '--',
    bat_level: '--',
    net_dl: '--',
    net_ul: '--',
    screen_hz: '--',
    timestamp: 0
  })

  const daemonRunning = ref(false)
  const daemonPid = ref('')
  const overlayRunning = ref(false)
  const overlayPid = ref('')

  const deviceInfo = ref({
    model: 'Android Device',
    brand: '',
    androidVer: '',
    kernelVer: '',
    soc: ''
  })

  const logs = ref('')
  let saveDebounceTimer = null
  let statsPollingTimer = null
  let isFetchingStats = false
  let isSavingConfig = false
  let hasPendingSave = false

  async function loadConfig() {
    const data = await readStateFile('config.json')
    if (data && typeof data === 'object') {
      config.value = { ...config.value, ...data }
    }
  }

  async function loadPosition() {
    const pos = await readStateFile('position.json')
    if (pos && typeof pos === 'object' && pos.x !== undefined && pos.y !== undefined) {
      if (pos.y < 0) pos.y = 0
      position.value = pos
    }
  }

  async function saveConfig() {
    if (isSavingConfig) {
      hasPendingSave = true
      return
    }
    isSavingConfig = true
    try {
      await writeStateFile('config.json', config.value)
    } finally {
      isSavingConfig = false
      if (hasPendingSave) {
        hasPendingSave = false
        saveConfig()
      }
    }
  }

  function triggerInstantSave() {
    if (saveDebounceTimer) clearTimeout(saveDebounceTimer)
    saveDebounceTimer = setTimeout(() => {
      saveConfig()
    }, 200)
  }

  async function savePosition() {
    if (position.value.y < 0) position.value.y = 0
    await writeStateFile('position.json', position.value)
  }

  function toggleOverlay() {
    config.value.visible = !config.value.visible
    saveConfig()
  }

  function resetPosition() {
    position.value.x = 565
    position.value.y = 156
    savePosition()
  }

  function setOrientation(isHorizontal) {
    config.value.is_horizontal = isHorizontal
    if (isHorizontal && config.value.bg_height > 70) {
      config.value.bg_width = 250
      config.value.bg_height = 56
    } else if (!isHorizontal && config.value.bg_width > 200) {
      config.value.bg_width = 150
      config.value.bg_height = 160
    }
    saveConfig()
  }

  function setAlignment(alignType) {
    config.value.align = alignType
    saveConfig()
  }

  function applyPreset(type) {
    const isHoriz = config.value.is_horizontal !== false
    if (type === 'compact') {
      config.value.show_fps = true
      config.value.show_cpu = true
      config.value.show_cpu_freq = true
      config.value.show_gov = false
      config.value.show_gpu = true
      config.value.show_gpu_freq = true
      config.value.show_gpu_gov = false
      config.value.show_ram = false
      config.value.show_battery = true
      config.value.show_net = false
      if (isHoriz) {
        config.value.bg_width = 250
        config.value.bg_height = 56
      } else {
        config.value.bg_width = 150
        config.value.bg_height = 120
      }
    } else if (type === 'minimal') {
      config.value.show_fps = true
      config.value.show_cpu = false
      config.value.show_cpu_freq = false
      config.value.show_gov = false
      config.value.show_gpu = false
      config.value.show_gpu_freq = false
      config.value.show_gpu_gov = false
      config.value.show_ram = false
      config.value.show_battery = false
      config.value.show_net = false
      if (isHoriz) {
        config.value.bg_width = 180
        config.value.bg_height = 42
      } else {
        config.value.bg_width = 130
        config.value.bg_height = 70
      }
    } else if (type === 'detailed') {
      config.value.show_fps = true
      config.value.show_cpu = true
      config.value.show_cpu_freq = true
      config.value.show_gov = true
      config.value.show_gpu = true
      config.value.show_gpu_freq = true
      config.value.show_gpu_gov = true
      config.value.show_ram = true
      config.value.show_battery = true
      config.value.show_net = true
      if (isHoriz) {
        config.value.bg_width = 320
        config.value.bg_height = 68
      } else {
        config.value.bg_width = 150
        config.value.bg_height = 160
      }
    }
    saveConfig()
  }

  async function fetchStats() {
    if (isFetchingStats) return
    isFetchingStats = true
    try {
      const data = await readStateFile('stats.json')
      if (data && typeof data === 'object') {
        stats.value = { ...stats.value, ...data }
      }
    } catch (e) {
    } finally {
      isFetchingStats = false
    }
  }

  async function checkProcesses() {
    try {
      const dPid = await execCommand('pidof fpsmoon_daemon 2>/dev/null || pgrep -x fpsmoon_daemon 2>/dev/null')
      daemonPid.value = dPid ? dPid.trim().split(' ')[0] : ''
      daemonRunning.value = !!daemonPid.value

      const oPid = await execCommand('pgrep -f com.fpsmoon.FPSMoonOverlay 2>/dev/null')
      overlayPid.value = oPid ? oPid.trim().split('\n')[0] : ''
      overlayRunning.value = !!overlayPid.value
    } catch (e) {}
  }

  async function restartService() {
    try {
      await execCommand('sh /data/adb/modules/fps_moon/action.sh >/dev/null 2>&1')
      await checkProcesses()
    } catch (e) {}
  }

  async function loadLogs() {
    try {
      const out = await execCommand('tail -n 60 /data/adb/modules/fps_moon/state/daemon.log 2>/dev/null; echo "\n--- Overlay Log ---"; tail -n 60 /data/adb/modules/fps_moon/state/overlay.log 2>/dev/null')
      logs.value = out || 'No logs recorded.'
    } catch (e) {
      logs.value = 'Failed to load logs.'
    }
  }

  async function clearLogs() {
    try {
      await execCommand('> /data/adb/modules/fps_moon/state/daemon.log 2>/dev/null; > /data/adb/modules/fps_moon/state/overlay.log 2>/dev/null')
      logs.value = 'Logs cleared.'
    } catch (e) {}
  }

  async function loadDeviceInfo() {
    try {
      const model = await execCommand('getprop ro.product.model 2>/dev/null')
      const brand = await execCommand('getprop ro.product.brand 2>/dev/null')
      const android = await execCommand('getprop ro.build.version.release 2>/dev/null')
      const kernel = await execCommand('uname -r 2>/dev/null')
      const soc = await execCommand('getprop ro.soc.model 2>/dev/null || getprop ro.board.platform 2>/dev/null')

      if (model && model.trim()) deviceInfo.value.model = model.trim()
      if (brand && brand.trim()) deviceInfo.value.brand = brand.trim()
      if (android && android.trim()) deviceInfo.value.androidVer = `Android ${android.trim()}`
      if (kernel && kernel.trim()) deviceInfo.value.kernelVer = kernel.trim()
      if (soc && soc.trim()) deviceInfo.value.soc = soc.trim()
    } catch (e) {}
  }

  function startPolling() {
    if (statsPollingTimer) return
    fetchStats()
    statsPollingTimer = setInterval(() => {
      fetchStats()
    }, 1200)
  }

  function stopPolling() {
    if (statsPollingTimer) {
      clearInterval(statsPollingTimer)
      statsPollingTimer = null
    }
  }

  return {
    moduleVersion,
    config,
    position,
    stats,
    daemonRunning,
    daemonPid,
    overlayRunning,
    overlayPid,
    deviceInfo,
    logs,
    loadConfig,
    loadPosition,
    saveConfig,
    triggerInstantSave,
    savePosition,
    toggleOverlay,
    resetPosition,
    setOrientation,
    setAlignment,
    applyPreset,
    fetchStats,
    checkProcesses,
    restartService,
    loadLogs,
    clearLogs,
    loadDeviceInfo,
    startPolling,
    stopPolling
  }
})
