<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">About</div>
        <div class="page-header-sub">System &amp; Engine Overview</div>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="content-area">
      <!-- About Hero Showcase Card -->
      <div class="about-hero-card">
        <div class="hero-top-row">
          <div class="tag-pill">
            <span>Pure Android WM</span>
          </div>
          <span class="badge-pill purple">{{ store.moduleVersion }} · Stable</span>
        </div>

        <div class="hero-center">
          <div class="hero-app-title">FPS Moon</div>
          <div class="hero-app-desc">
            Ultra-low latency real-time performance HUD &amp; telemetry overlay
          </div>
          <div class="hero-author-line">
            <span>Crafted with precision by</span>
            <a href="https://github.com/itswill00/fpsmoon" target="_blank" class="author-link" @click.stop="openGithub">
              @itswill00
            </a>
          </div>
        </div>

        <!-- Engine Specs Chips -->
        <div class="about-chips-grid">
          <div class="spec-chip">
            <span class="spec-label">Engine</span>
            <span class="spec-val">Skia CPU Canvas</span>
          </div>
          <div class="spec-chip">
            <span class="spec-label">Leash</span>
            <span class="spec-val">Pure Java WM</span>
          </div>
          <div class="spec-chip">
            <span class="spec-label">Telemetry</span>
            <span class="spec-val">C-Daemon (Zero-OOM)</span>
          </div>
          <div class="spec-chip">
            <span class="spec-label">Platform</span>
            <span class="spec-val">AOSP &amp; HyperOS</span>
          </div>
        </div>
      </div>

      <!-- Device & System Specs -->
      <div class="section-title">Device &amp; System</div>
      <div class="md3-list-group">
        <div class="md3-list-row">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="about" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Device Model</div>
              <div class="row-sub">{{ store.deviceInfo.brand ? store.deviceInfo.brand + ' ' : '' }}{{ store.deviceInfo.model }}</div>
            </div>
          </div>
          <span class="row-val">{{ store.deviceInfo.model }}</span>
        </div>

        <div class="md3-list-row">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="shield" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Android System</div>
              <div class="row-sub">Operating System Version</div>
            </div>
          </div>
          <span class="row-val">{{ store.deviceInfo.androidVer || 'Android' }}</span>
        </div>

        <div class="md3-list-row">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="cpu" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Kernel Version</div>
              <div class="row-sub">Linux kernel release</div>
            </div>
          </div>
          <span class="row-val" style="font-size: 10.5px;">{{ store.deviceInfo.kernelVer || '—' }}</span>
        </div>

        <div class="md3-list-row">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="screen" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Display Refresh Rate</div>
              <div class="row-sub">Hardware screen Hz</div>
            </div>
          </div>
          <span class="row-val">{{ store.stats.screen_hz ? store.stats.screen_hz + ' Hz' : '60 Hz' }}</span>
        </div>
      </div>

      <!-- Release Notes (Expandable Changelog) -->
      <div class="section-title">Release Notes</div>
      <div class="md3-list-group">
        <div class="changelog-card">
          <div class="release-header">
            <div class="release-ver">v1.0.1 (Latest)</div>
            <span class="badge-pill green">Active</span>
          </div>
          <ul class="changelog-list">
            <li><strong>Universal AOSP &amp; Custom ROM Support</strong>: Added native Typeface reflection fallback to resolve Minikin SIGABRT crashes on vanilla Android 13/14+.</li>
            <li><strong>libbinder Thread Pool Initializer</strong>: Added `BinderInternal.joinThreadPool()` to resolve WindowManager leash initialization timeouts.</li>
            <li><strong>Skia CPU Software Rendering</strong>: Bypassed SELinux GPU device node restrictions on rooted AOSP builds.</li>
            <li><strong>Dynamic AppOps Escalation</strong>: Automatic grant of `SYSTEM_ALERT_WINDOW` across UID 0, 1000, 2000, root, and shell.</li>
            <li><strong>Vue 3 Modern WebUI</strong>: Complete overhaul to single-file Vue 3 architecture matching HyperCore standards.</li>
          </ul>
        </div>
      </div>

      <div class="footer-note">
        FPS Moon • Developed by <strong>@itswill00</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useFpsMoonStore } from '@/stores/fpsmoon'
import { openExternal } from '@/helpers/shell'
import Icons from '@/components/icons/Icons.vue'

const store = useFpsMoonStore()

function openGithub() {
  openExternal('https://github.com/itswill00/fpsmoon')
}

onMounted(() => {
  store.loadDeviceInfo()
})
</script>

<style scoped>
.about-hero-card {
  background: var(--surface-container);
  border: 1px solid var(--surface-container-high);
  border-radius: 18px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.hero-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-pill {
  font-size: 10.5px;
  font-weight: 600;
  color: var(--primary);
  background: var(--primary-container);
  padding: 3px 9px;
  border-radius: 10px;
  border: 1px solid rgba(167, 139, 250, 0.25);
}

.hero-center {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-app-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--on-surface);
  letter-spacing: -0.3px;
}

.hero-app-desc {
  font-size: 12px;
  color: var(--on-surface-variant);
  line-height: 1.4;
}

.hero-author-line {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  color: var(--on-surface-variant);
  margin-top: 4px;
}

.author-link {
  color: var(--primary);
  text-decoration: none;
  font-weight: 600;
}

.author-link:active {
  text-decoration: underline;
}

.about-chips-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.spec-chip {
  background: var(--surface-container-low);
  border: 1px solid var(--outline-variant);
  border-radius: 10px;
  padding: 6px 10px;
  display: flex;
  flex-direction: column;
}

.spec-label {
  font-size: 9.5px;
  color: var(--on-surface-variant);
  opacity: 0.75;
}

.spec-val {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--on-surface);
}

.changelog-card {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.release-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.release-ver {
  font-size: 13px;
  font-weight: 700;
  color: var(--on-surface);
}

.changelog-list {
  padding-left: 18px;
  font-size: 11.5px;
  color: var(--on-surface-variant);
  line-height: 1.6;
}

.changelog-list li {
  margin-bottom: 4px;
}

.changelog-list strong {
  color: var(--on-surface);
}

.footer-note {
  text-align: center;
  font-size: 11px;
  color: var(--on-surface-variant);
  opacity: 0.6;
  padding: 16px 0 10px 0;
}
</style>
