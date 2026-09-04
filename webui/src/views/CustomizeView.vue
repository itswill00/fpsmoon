<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">Customize</div>
        <div class="page-header-sub">Display Metrics &amp; Appearance</div>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="content-area">
      <!-- Items To Display -->
      <div class="section-title">Items to display</div>
      <div class="md3-list-group">
        <!-- FPS & Screen Hz -->
        <div class="md3-list-row clickable" @click="toggleItem('show_fps')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="fps" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Frame rate and refresh rate</div>
              <div class="row-sub">Game FPS, frametime, and screen Hz</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_fps" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- CPU Load -->
        <div class="md3-list-row clickable" @click="toggleItem('show_cpu')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="cpu" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Processor (CPU)</div>
              <div class="row-sub">CPU load and temperature</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_cpu" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- CPU Frequency -->
        <div class="md3-list-row clickable" @click="toggleItem('show_cpu_freq')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="chip" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">CPU frequency</div>
              <div class="row-sub">Show current CPU clock speed</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_cpu_freq" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- CPU Governor -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gov')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="tune" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">CPU governor</div>
              <div class="row-sub">Active governor and policy range</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gov" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- GPU Load -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gpu')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="gpu" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Graphics card (GPU)</div>
              <div class="row-sub">GPU load and temperature</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gpu" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- GPU Frequency -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gpu_freq')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="zap" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">GPU frequency</div>
              <div class="row-sub">Show current GPU clock speed</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gpu_freq" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- GPU Governor -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gpu_gov')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="sliders" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">GPU governor and policy</div>
              <div class="row-sub">Active GPU governor and frequency levels</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gpu_gov" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- RAM Memory -->
        <div class="md3-list-row clickable" @click="toggleItem('show_ram')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="memory" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">RAM memory</div>
              <div class="row-sub">Used and total memory</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_ram" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Battery & Power -->
        <div class="md3-list-row clickable" @click="toggleItem('show_battery')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="battery" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Battery and power</div>
              <div class="row-sub">Power usage, current, and temperature</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_battery" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Network Speed -->
        <div class="md3-list-row clickable" @click="toggleItem('show_net')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="net" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Internet speed</div>
              <div class="row-sub">Download and upload speed</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_net" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>
      </div>

      <!-- Style and Card Size Sliders -->
      <div class="section-title">Style and card size</div>
      <div class="md3-list-group">
        <!-- Card Width -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Card width</span>
            <span class="slider-val-badge">{{ store.config.bg_width || 250 }}px</span>
          </div>
          <input
            type="range"
            min="150"
            max="400"
            v-model.number="store.config.bg_width"
            @input="onSliderInput"
            class="md3-range-slider"
          />
        </div>

        <!-- Card Height -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Card height</span>
            <span class="slider-val-badge">{{ store.config.bg_height || 56 }}px</span>
          </div>
          <input
            type="range"
            min="40"
            max="160"
            v-model.number="store.config.bg_height"
            @input="onSliderInput"
            class="md3-range-slider"
          />
        </div>

        <!-- Overall Scale -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Overall scale</span>
            <span class="slider-val-badge">{{ (store.config.scale || 1.0).toFixed(1) }}x</span>
          </div>
          <input
            type="range"
            min="50"
            max="180"
            :value="Math.round((store.config.scale || 1.0) * 100)"
            @input="e => onScaleInput(e.target.value)"
            class="md3-range-slider"
          />
        </div>

        <!-- Text Size -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Text size</span>
            <span class="slider-val-badge">{{ store.config.font_size || 12 }}sp</span>
          </div>
          <input
            type="range"
            min="9"
            max="22"
            v-model.number="store.config.font_size"
            @input="onSliderInput"
            class="md3-range-slider"
          />
        </div>

        <!-- Corner Radius -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Corner radius</span>
            <span class="slider-val-badge">{{ store.config.corner_radius || 14 }}px</span>
          </div>
          <input
            type="range"
            min="0"
            max="24"
            v-model.number="store.config.corner_radius"
            @input="onSliderInput"
            class="md3-range-slider"
          />
        </div>

        <!-- Refresh Interval -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Refresh interval</span>
            <span class="slider-val-badge">{{ store.config.refresh_interval || 250 }}ms</span>
          </div>
          <input
            type="range"
            min="100"
            max="1000"
            step="50"
            v-model.number="store.config.refresh_interval"
            @input="onSliderInput"
            class="md3-range-slider"
          />
        </div>

        <!-- Transparency / Opacity -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Transparency</span>
            <span class="slider-val-badge">{{ Math.round((store.config.opacity || 0.85) * 100) }}%</span>
          </div>
          <input
            type="range"
            min="10"
            max="100"
            :value="Math.round((store.config.opacity || 0.85) * 100)"
            @input="e => onOpacityInput(e.target.value)"
            class="md3-range-slider"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import { useFpsMoonStore } from '@/stores/fpsmoon'
import Icons from '@/components/icons/Icons.vue'

const store = useFpsMoonStore()
const toast = inject('toast')

function toggleItem(key) {
  store.config[key] = !store.config[key]
  store.saveConfig()
  toast('Settings updated')
}

function onToggleChange() {
  store.saveConfig()
  toast('Settings updated')
}

function onSliderInput() {
  store.triggerInstantSave()
}

function onScaleInput(val) {
  store.config.scale = parseInt(val) / 100.0
  store.triggerInstantSave()
}

function onOpacityInput(val) {
  store.config.opacity = parseInt(val) / 100.0
  store.triggerInstantSave()
}
</script>

<style scoped>
.slider-row {
  padding: 14px 16px;
  border-bottom: 1px solid var(--surface-container-high);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.slider-row:last-child {
  border-bottom: none;
}

.slider-label-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.slider-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--on-surface);
}

.slider-val-badge {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--primary);
  font-variant-numeric: tabular-nums;
  background: var(--surface-container-high);
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid var(--outline-variant);
}
</style>
