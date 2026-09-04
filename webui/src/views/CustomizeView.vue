<template>
  <div style="height: 100%; display: flex; flex-direction: column;">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">Customize</div>
        <div class="page-header-sub">Display items and card appearance</div>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="content-area">
      <!-- Display Items Section -->
      <div class="section-title">Display items</div>
      <div class="md3-list-group">
        <!-- Frame Rate & Screen Refresh -->
        <div class="md3-list-row clickable" @click="toggleItem('show_fps')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="fps" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Frame rate</div>
              <div class="row-sub">Frames per second and display refresh rate</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_fps" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Processor (CPU) -->
        <div class="md3-list-row clickable" @click="toggleItem('show_cpu')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="cpu" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Processor</div>
              <div class="row-sub">CPU usage and temperature</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_cpu" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- CPU Clock Speed -->
        <div class="md3-list-row clickable" @click="toggleItem('show_cpu_freq')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="chip" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">CPU clock speed</div>
              <div class="row-sub">Active processor frequency</div>
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
              <div class="row-sub">Scaling governor and policy range</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gov" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Graphics (GPU) -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gpu')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="gpu" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Graphics</div>
              <div class="row-sub">GPU usage and temperature</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gpu" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- GPU Clock Speed -->
        <div class="md3-list-row clickable" @click="toggleItem('show_gpu_freq')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="zap" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">GPU clock speed</div>
              <div class="row-sub">Active graphics processor frequency</div>
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
              <div class="row-title">GPU governor</div>
              <div class="row-sub">Active graphics governor and policy</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_gpu_gov" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Memory (RAM) -->
        <div class="md3-list-row clickable" @click="toggleItem('show_ram')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="memory" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Memory</div>
              <div class="row-sub">Used and total system RAM</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_ram" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Compressed Memory (ZRAM) -->
        <div class="md3-list-row clickable" @click="toggleItem('show_zram')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="chip" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">ZRAM swap</div>
              <div class="row-sub">Compressed memory usage</div>
            </div>
          </div>
          <label class="md3-switch" @click.stop>
            <input type="checkbox" v-model="store.config.show_zram" @change="onToggleChange" />
            <span class="md3-switch-track">
              <span class="md3-switch-thumb"></span>
            </span>
          </label>
        </div>

        <!-- Battery -->
        <div class="md3-list-row clickable" @click="toggleItem('show_battery')">
          <div class="row-left">
            <div class="icon-badge">
              <Icons name="battery" :size="18" />
            </div>
            <div class="row-meta">
              <div class="row-title">Battery</div>
              <div class="row-sub">Power usage, current draw, and temperature</div>
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
              <div class="row-title">Network speed</div>
              <div class="row-sub">Download and upload rate</div>
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

      <!-- Appearance Sliders Section -->
      <div class="section-title">Appearance</div>
      <div class="md3-list-group">
        <!-- Card Width -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Card width</span>
            <span class="slider-val-badge">{{ store.config.bg_width || 150 }}px</span>
          </div>
          <input
            type="range"
            min="100"
            max="400"
            step="2"
            v-model.number="store.config.bg_width"
            @input="onSliderInput"
            @change="onSliderChange"
            class="md3-range-slider"
          />
        </div>

        <!-- Card Height -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Card height</span>
            <span class="slider-val-badge">{{ store.config.bg_height || 160 }}px</span>
          </div>
          <input
            type="range"
            min="36"
            max="260"
            step="2"
            v-model.number="store.config.bg_height"
            @input="onSliderInput"
            @change="onSliderChange"
            class="md3-range-slider"
          />
        </div>

        <!-- Scale -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Scale</span>
            <span class="slider-val-badge">{{ (store.config.scale || 0.79).toFixed(2) }}x</span>
          </div>
          <input
            type="range"
            min="50"
            max="180"
            :value="Math.round((store.config.scale || 0.79) * 100)"
            @input="e => onScaleInput(e.target.value)"
            @change="onSliderChange"
            class="md3-range-slider"
          />
        </div>

        <!-- Text Size -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Text size</span>
            <span class="slider-val-badge">{{ store.config.font_size || 11 }}sp</span>
          </div>
          <input
            type="range"
            min="9"
            max="22"
            v-model.number="store.config.font_size"
            @input="onSliderInput"
            @change="onSliderChange"
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
            @change="onSliderChange"
            class="md3-range-slider"
          />
        </div>

        <!-- Update Interval -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Update speed</span>
            <span class="slider-val-badge">{{ store.config.refresh_interval || 850 }}ms</span>
          </div>
          <input
            type="range"
            min="100"
            max="1500"
            step="50"
            v-model.number="store.config.refresh_interval"
            @input="onSliderInput"
            @change="onSliderChange"
            class="md3-range-slider"
          />
        </div>

        <!-- Transparency -->
        <div class="slider-row">
          <div class="slider-label-line">
            <span class="slider-title">Transparency</span>
            <span class="slider-val-badge">{{ Math.round((store.config.opacity || 0.58) * 100) }}%</span>
          </div>
          <input
            type="range"
            min="10"
            max="100"
            :value="Math.round((store.config.opacity || 0.58) * 100)"
            @input="e => onOpacityInput(e.target.value)"
            @change="onSliderChange"
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
  toast('Settings saved')
}

function onToggleChange() {
  store.saveConfig()
  toast('Settings saved')
}

function onSliderInput() {
  store.triggerInstantSave()
}

function onSliderChange() {
  store.saveConfig()
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
  color: var(--on-surface);
  font-variant-numeric: tabular-nums;
  background: var(--surface-container-high);
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid var(--outline-variant);
}
</style>
