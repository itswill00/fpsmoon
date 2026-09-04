<template>
  <div style="height: 100%; display: flex; flex-direction: column;" @click="menuOpen = false">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">Activity Log</div>
        <div class="page-header-sub">Service status and console output</div>
      </div>
      <div class="menu-container" @click.stop>
        <button class="btn-icon" @click="menuOpen = !menuOpen" title="Options">
          <Icons name="more-vertical" :size="18" />
        </button>

        <Transition name="menu-pop">
          <div v-if="menuOpen" class="dropdown-menu">
            <button class="menu-item" @click="handleCopyLogs">
              <Icons name="copy" :size="14" />
              <span>Copy log</span>
            </button>
            <button class="menu-item" @click="handleRestartService">
              <Icons name="refresh" :size="14" />
              <span>Restart service</span>
            </button>
            <div class="menu-divider"></div>
            <button class="menu-item danger" @click="handleClearLogs">
              <Icons name="trash" :size="14" />
              <span>Clear log</span>
            </button>
          </div>
        </Transition>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="content-area" style="display: flex; flex-direction: column;">
      <!-- Service Diagnostic Status Cards -->
      <div class="stat-grid-2" style="margin-bottom: 12px;">
        <div class="stat-box">
          <span class="stat-label">Background service</span>
          <span class="stat-val">
            {{ store.daemonRunning ? `Active (PID ${store.daemonPid})` : 'Standby' }}
          </span>
          <span class="stat-sub">{{ store.daemonRunning ? 'Running' : 'Stopped' }}</span>
        </div>
        <div class="stat-box">
          <span class="stat-label">Screen overlay</span>
          <span class="stat-val">
            {{ store.overlayRunning ? `Active (PID ${store.overlayPid})` : 'Standby' }}
          </span>
          <span class="stat-sub">{{ store.overlayRunning ? 'Running' : 'Stopped' }}</span>
        </div>
      </div>

      <!-- Terminal Log Viewport -->
      <div class="terminal-container">
        <div class="terminal-bar">
          <div class="terminal-title">Console</div>
          <button class="refresh-btn" @click="fetchLogs">
            <Icons name="refresh" :size="13" />
            <span>Refresh</span>
          </button>
        </div>
        <div ref="terminalBody" class="terminal-body">
          <pre class="terminal-text">{{ store.logs || 'No recent log messages.' }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, nextTick } from 'vue'
import { useFpsMoonStore } from '@/stores/fpsmoon'
import Icons from '@/components/icons/Icons.vue'

const store = useFpsMoonStore()
const toast = inject('toast')
const menuOpen = ref(false)
const terminalBody = ref(null)

async function fetchLogs() {
  await store.loadLogs()
  await nextTick()
  if (terminalBody.value) {
    terminalBody.value.scrollTop = terminalBody.value.scrollHeight
  }
}

async function handleCopyLogs() {
  menuOpen.value = false
  if (!store.logs) return
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(store.logs)
      toast('Log copied to clipboard')
    } else {
      const el = document.createElement('textarea')
      el.value = store.logs
      document.body.appendChild(el)
      el.select()
      document.execCommand('copy')
      document.body.removeChild(el)
      toast('Log copied to clipboard')
    }
  } catch (e) {
    toast('Failed to copy log')
  }
}

async function handleRestartService() {
  menuOpen.value = false
  toast('Restarting services...')
  await store.restartService()
  await store.loadLogs()
  toast('Services restarted')
}

async function handleClearLogs() {
  menuOpen.value = false
  await store.clearLogs()
  toast('Log cleared')
}

onMounted(() => {
  store.checkProcesses()
  fetchLogs()
})
</script>

<style scoped>
.menu-container {
  position: relative;
}

.btn-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--surface-container-high);
  border: 1px solid var(--outline-variant);
  color: var(--on-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-icon:active {
  background: var(--surface-bright);
}

.dropdown-menu {
  position: absolute;
  top: 42px;
  right: 0;
  width: 160px;
  background: var(--surface-container-high);
  border: 1px solid var(--surface-bright);
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  padding: 6px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-pop-enter-active,
.menu-pop-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.menu-pop-enter-from,
.menu-pop-leave-to {
  opacity: 0;
  transform: scale(0.94) translateY(-4px);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border: none;
  background: transparent;
  color: var(--on-surface);
  font-size: 11.5px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.12s ease;
}

.menu-item:hover,
.menu-item:active {
  background: var(--surface-container-highest);
}

.menu-item.danger {
  color: var(--error);
}

.menu-divider {
  height: 1px;
  background: var(--surface-container-highest);
  margin: 4px 0;
}

.stat-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
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
  font-size: 14px;
  font-weight: 700;
  color: var(--on-surface);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.stat-sub {
  font-size: 10.5px;
  color: var(--on-surface-variant);
  margin-top: 3px;
}

.terminal-container {
  flex: 1;
  min-height: 320px;
  background: var(--surface-container-lowest);
  border: 1px solid var(--outline-variant);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.terminal-bar {
  background: var(--surface-container);
  border-bottom: 1px solid var(--surface-container-high);
  padding: 8px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.terminal-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--on-surface-variant);
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: var(--surface-container-high);
  border: 1px solid var(--outline-variant);
  border-radius: 8px;
  color: var(--on-surface);
  font-size: 10.5px;
  font-weight: 500;
  padding: 4px 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.refresh-btn:active {
  background: var(--surface-bright);
}

.terminal-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 11px;
  line-height: 1.45;
  color: var(--on-surface-variant);
}

.terminal-text {
  white-space: pre-wrap;
  word-break: break-all;
  font-family: var(--font-mono);
  color: #cbd5e1;
}
</style>
