<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">Dashboard</div>
        <div class="page-header-sub">Overlay status and controls</div>
      </div>
      <span class="badge-pill">{{ store.moduleVersion }}</span>
    </div>

    <!-- Scrollable Content Area -->
    <div class="content-area">
      <!-- App Hero Banner -->
      <div class="hero-banner">
        <div class="banner-top-row">
          <div class="app-banner-icon">
            <Icons name="moon" :size="22" />
          </div>
          <div class="banner-title-group">
            <div class="banner-title-line">
              <span class="app-banner-title">FPS Moon</span>
              <span class="badge-pill">
                {{ store.config.visible ? 'Active' : 'Disabled' }}
              </span>
            </div>
            <div class="app-banner-sub">Screen overlay by @itswill00</div>
          </div>
        </div>

        <!-- High-level Status Chips -->
        <div class="banner-stats-row">
          <div class="banner-chip">
            <span>Service: <strong>{{ store.config.visible ? 'Running' : 'Standby' }}</strong></span>
          </div>
          <div class="banner-chip">
            <span>Layout: <strong>{{ store.config.is_horizontal ? 'Horizontal' : 'Vertical' }}</strong></span>
          </div>
          <div class="banner-chip">
            <span>Align: <strong>{{ (store.config.align || 'left').toUpperCase() }}</strong></span>
          </div>
          <div class="banner-chip">
            <span>Scale: <strong>{{ Math.round((store.config.scale || 0.79) * 100) }}%</strong></span>
          </div>
          <div class="banner-chip">
            <span>Display: <strong>{{ store.stats.screen_hz ? store.stats.screen_hz + ' Hz' : '60 Hz' }}</strong></span>
          </div>
        </div>

        <!-- Quick Actions Row -->
        <div class="quick-actions-row">
          <button class="quick-act-btn" @click="handleReset">
            <Icons name="reset" :size="14" />
            <span>Reset position</span>
          </button>
          <button class="quick-act-btn" @click="handleRestart">
            <Icons name="refresh" :size="14" />
            <span>Restart service</span>
          </button>
          <button class="quick-act-btn" @click="goToLogs">
            <Icons name="logs" :size="14" />
            <span>Activity log</span>
          </button>
        </div>
      </div>

      <!-- Master Overlay Switch -->
      <div class="md3-list-group">
        <div class="md3-list-row clickable" @click="handleToggle">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="screen" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Screen overlay</div>
              <div class="row-sub">{{ store.config.visible ? 'Floating overlay displayed on screen' : 'Overlay hidden from screen' }}</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input
              type="checkbox"
              v-model="store.config.visible"
              @change="onConfigChange(store.config.visible ? 'Overlay enabled' : 'Overlay disabled')"
            />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>
      </div>

      <!-- Performance Stats Grid -->
      <div class="section-title">Performance</div>
      <div class="stat-grid-2">
        <div class="stat-box">
          <span class="stat-label">Frame rate</span>
          <span class="stat-val">{{ store.stats.fps !== '--' ? store.stats.fps + ' FPS' : '--' }}</span>
          <span class="stat-sub">{{ store.stats.frametime !== '--' ? store.stats.frametime + ' ms' : '--' }}</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">Processor</span>
          <span class="stat-val">{{ store.stats.cpu_load !== '--' ? store.stats.cpu_load + '%' : '--' }}</span>
          <span class="stat-sub">{{ (store.stats.cpu_freq || '--') + ' · ' + (store.stats.cpu_temp || '--') + '°C' }}</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">Graphics</span>
          <span class="stat-val">{{ store.stats.gpu_load !== '--' ? store.stats.gpu_load + '%' : '--' }}</span>
          <span class="stat-sub">{{ (store.stats.gpu_freq || '--') + ' · ' + (store.stats.gpu_temp || '--') + '°C' }}</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">Battery</span>
          <span class="stat-val">{{ store.stats.bat_watt !== '--' ? store.stats.bat_watt + ' W' : '--' }}</span>
          <span class="stat-sub">{{ store.stats.bat_temp ? store.stats.bat_temp + '°C' : '--' }}</span>
        </div>
      </div>

      <!-- Layout Style -->
      <div class="section-title">Layout</div>
      <div class="segmented-bar">
        <button
          class="segmented-btn"
          :class="{ active: store.config.is_horizontal !== false }"
          @click="changeOrientation(true)"
        >
          Horizontal pill
        </button>
        <button
          class="segmented-btn"
          :class="{ active: store.config.is_horizontal === false }"
          @click="changeOrientation(false)"
        >
          Vertical stack
        </button>
      </div>

      <!-- Alignment Controls -->
      <div class="segmented-bar">
        <button
          class="segmented-btn"
          :class="{ active: store.config.align === 'left' }"
          @click="changeAlignment('left')"
        >
          Left
        </button>
        <button
          class="segmented-btn"
          :class="{ active: store.config.align === 'center' }"
          @click="changeAlignment('center')"
        >
          Center
        </button>
        <button
          class="segmented-btn"
          :class="{ active: store.config.align === 'right' }"
          @click="changeAlignment('right')"
        >
          Right
        </button>
      </div>

      <!-- Presets -->
      <div class="section-title">Presets</div>
      <div class="segmented-bar">
        <button
          class="segmented-btn"
          :class="{ active: activePreset === 'compact' }"
          @click="changePreset('compact')"
        >
          Compact
        </button>
        <button
          class="segmented-btn"
          :class="{ active: activePreset === 'minimal' }"
          @click="changePreset('minimal')"
        >
          Minimal
        </button>
        <button
          class="segmented-btn"
          :class="{ active: activePreset === 'detailed' }"
          @click="changePreset('detailed')"
        >
          Detailed
        </button>
      </div>

      <!-- Footer Branding -->
      <div class="footer-note">
        FPS Moon • Developed by <strong>@itswill00</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useFpsMoonStore } from '@/stores/fpsmoon'
import Icons from '@/components/icons/Icons.vue'

const store = useFpsMoonStore()
const router = useRouter()
const toast = inject('toast')

const activePreset = computed(() => {
  const c = store.config
  if (
    c.show_fps &&
    !c.show_cpu &&
    !c.show_cpu_freq &&
    !c.show_gov &&
    !c.show_gpu &&
    !c.show_gpu_freq &&
    !c.show_gpu_gov &&
    !c.show_ram &&
    !c.show_zram &&
    !c.show_battery &&
    !c.show_net
  ) {
    return 'minimal'
  }
  if (
    c.show_fps &&
    c.show_cpu &&
    c.show_cpu_freq &&
    !c.show_gov &&
    c.show_gpu &&
    c.show_gpu_freq &&
    !c.show_gpu_gov &&
    !c.show_ram &&
    !c.show_zram &&
    c.show_battery &&
    !c.show_net
  ) {
    return 'compact'
  }
  if (
    c.show_fps &&
    c.show_cpu &&
    c.show_cpu_freq &&
    c.show_gov &&
    c.show_gpu &&
    c.show_gpu_freq &&
    c.show_gpu_gov &&
    c.show_ram &&
    !c.show_zram &&
    c.show_battery &&
    !c.show_net
  ) {
    return 'detailed'
  }
  return null
})

function handleToggle() {
  store.toggleOverlay()
  toast(store.config.visible ? 'Overlay enabled' : 'Overlay disabled')
}

function handleReset() {
  store.resetPosition()
  toast('Position reset to default')
}

async function handleRestart() {
  toast('Restarting services...')
  await store.restartService()
  toast('Services restarted')
}

function goToLogs() {
  router.push('/logs')
}

function onConfigChange(msg) {
  store.saveConfig()
  if (msg) toast(msg)
}

function changeOrientation(val) {
  store.setOrientation(val)
  toast(val ? 'Horizontal layout' : 'Vertical layout')
}

function changeAlignment(align) {
  store.setAlignment(align)
  toast(`Aligned to ${align}`)
}

function changePreset(type) {
  store.applyPreset(type)
  const label = type.charAt(0).toUpperCase() + type.slice(1)
  toast(`${label} preset applied`)
}
</script>

<style scoped>
.hero-banner {
  background: var(--surface-container);
  border: 1px solid var(--surface-container-high);
  border-radius: 18px;
  padding: 16px;
  margin-bottom: 14px;
}

.banner-top-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.app-banner-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: var(--surface-container-high);
  border: 1px solid var(--outline-variant);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--on-surface);
  flex-shrink: 0;
}

.banner-title-group {
  min-width: 0;
  flex: 1;
}

.banner-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-banner-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--on-surface);
}

.app-banner-sub {
  font-size: 11.5px;
  color: var(--on-surface-variant);
  margin-top: 2px;
}

.banner-stats-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.banner-chip {
  background: var(--surface-container-low);
  border: 1px solid var(--outline-variant);
  padding: 4px 10px;
  border-radius: 9px;
  font-size: 11px;
  color: var(--on-surface-variant);
}

.banner-chip strong {
  color: var(--on-surface);
  font-variant-numeric: tabular-nums;
}

.quick-actions-row {
  display: flex;
  gap: 8px;
}

.quick-act-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: var(--surface-container-high);
  border: 1px solid var(--outline-variant);
  border-radius: 10px;
  padding: 8px 10px;
  color: var(--on-surface);
  font-size: 11.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.quick-act-btn:active {
  background: var(--surface-bright);
  transform: scale(0.97);
}

.stat-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 14px;
}

.stat-box {
  background: var(--surface-container);
  border: 1px solid var(--surface-container-high);
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 10.5px;
  font-weight: 500;
  color: var(--on-surface-variant);
  margin-bottom: 3px;
}

.stat-val {
  font-size: 17px;
  font-weight: 700;
  color: var(--on-surface);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.stat-sub {
  font-size: 11px;
  color: var(--on-surface-variant);
  margin-top: 3px;
  font-variant-numeric: tabular-nums;
}

.footer-note {
  text-align: center;
  font-size: 11px;
  color: var(--on-surface-variant);
  opacity: 0.5;
  padding: 16px 0 10px 0;
}
</style>
