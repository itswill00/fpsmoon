<template>
  <div
    class="app-shell"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
  >
    <main style="flex: 1; overflow: hidden; position: relative;">
      <router-view v-slot="{ Component }">
        <transition :name="transitionName" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Bottom Navigation Dock -->
    <Navigation />

    <!-- Modern Toast Pill -->
    <transition name="toast-slide">
      <div v-if="toastMsg" class="toast-pill">
        <Icons name="check" :size="14" style="color: var(--primary);" />
        <span>{{ toastMsg }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, provide, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFpsMoonStore } from '@/stores/fpsmoon'
import Navigation from '@/components/Navigation.vue'
import Icons from '@/components/icons/Icons.vue'

const store = useFpsMoonStore()
const route = useRoute()
const router = useRouter()

const toastMsg = ref('')
const transitionName = ref('slide-left')
let toastTimer = null

const routesOrder = ['/', '/customize', '/logs', '/about']

let touchStartX = 0
let touchStartY = 0

function onTouchStart(e) {
  if (e.touches && e.touches.length === 1) {
    touchStartX = e.touches[0].clientX
    touchStartY = e.touches[0].clientY
  }
}

function onTouchEnd(e) {
  if (!e.changedTouches || e.changedTouches.length !== 1) return

  // Disable swipe on logs so user can scroll horizontally if needed
  if (route.path === '/logs') return

  const touchEndX = e.changedTouches[0].clientX
  const touchEndY = e.changedTouches[0].clientY
  const deltaX = touchEndX - touchStartX
  const deltaY = touchEndY - touchStartY

  if (Math.abs(deltaX) > Math.abs(deltaY) * 1.4 && Math.abs(deltaX) > 55) {
    const currentIdx = routesOrder.indexOf(route.path)
    if (currentIdx === -1) return

    if (deltaX < 0 && currentIdx < routesOrder.length - 1) {
      transitionName.value = 'slide-left'
      router.push(routesOrder[currentIdx + 1])
    } else if (deltaX > 0 && currentIdx > 0) {
      transitionName.value = 'slide-right'
      router.push(routesOrder[currentIdx - 1])
    }
  }
}

watch(
  () => route.path,
  (to, from) => {
    const toIdx = routesOrder.indexOf(to)
    const fromIdx = routesOrder.indexOf(from)
    if (toIdx !== -1 && fromIdx !== -1) {
      transitionName.value = toIdx > fromIdx ? 'slide-left' : 'slide-right'
    }
  }
)

function toast(msg) {
  toastMsg.value = msg
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toastMsg.value = ''
  }, 2200)
}

provide('toast', toast)

function handleVisibilityChange() {
  if (document.hidden) {
    store.stopPolling()
  } else {
    store.startPolling()
  }
}

onMounted(() => {
  store.loadConfig()
  store.loadPosition()
  store.startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  store.stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style>
.slide-left-enter-active,
.slide-right-enter-active {
  transition: transform 0.26s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.2s ease;
  position: absolute;
  width: 100%;
  height: 100%;
  will-change: transform, opacity;
}

.slide-left-leave-active,
.slide-right-leave-active {
  transition: transform 0.2s cubic-bezier(0.4, 0, 1, 1), opacity 0.16s ease;
  position: absolute;
  width: 100%;
  height: 100%;
  will-change: transform, opacity;
}

.slide-left-enter-from {
  opacity: 0;
  transform: translate3d(24px, 0, 0) scale(0.985);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translate3d(-18px, 0, 0) scale(0.99);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translate3d(-24px, 0, 0) scale(0.985);
}
.slide-right-leave-to {
  opacity: 0;
  transform: translate3d(18px, 0, 0) scale(0.99);
}
</style>
