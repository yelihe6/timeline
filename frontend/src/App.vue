<template>
  <div class="app-root">
    <Toast />
    <ConfirmModal />
    <div
      v-if="currentImageItem?.url"
      class="cover-layer"
      :style="coverStyle"
    ></div>
    <div class="app-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { useCover } from './composables/useCover'
import Toast from './components/Toast.vue'
import ConfirmModal from './components/ConfirmModal.vue'

const { coverOpacity, coverImages, coverCarouselInterval } = useCover()

const currentIndex = ref(0)
let carouselTimer = null

const currentImageItem = computed(() => {
  const imgs = coverImages.value
  if (!imgs?.length) return null
  const idx = currentIndex.value % imgs.length
  return imgs[idx] || null
})

const coverStyle = computed(() => {
  const item = currentImageItem.value
  if (!item?.url) return { opacity: 0 }
  const pos = item.position || { x: 50, y: 50 }
  const sz = item.size ?? 100
  return {
    opacity: coverOpacity.value,
    backgroundImage: `url(${item.url})`,
    backgroundSize: sz === 100 ? 'cover' : `${sz}%`,
    backgroundPosition: `${pos.x}% ${pos.y}%`,
    backgroundRepeat: 'no-repeat'
  }
})

function startCarousel() {
  if (carouselTimer) clearInterval(carouselTimer)
  carouselTimer = null
  const imgs = coverImages.value
  const interval = coverCarouselInterval.value
  if (imgs.length > 1 && interval > 0) {
    carouselTimer = setInterval(() => {
      currentIndex.value = (currentIndex.value + 1) % imgs.length
    }, interval * 1000)
  }
}

function stopCarousel() {
  if (carouselTimer) {
    clearInterval(carouselTimer)
    carouselTimer = null
  }
}

watch([coverImages, coverCarouselInterval], () => {
  const imgs = coverImages.value
  if (imgs.length === 0) {
    currentIndex.value = 0
    stopCarousel()
  } else {
    currentIndex.value = Math.min(currentIndex.value, imgs.length - 1)
    startCarousel()
  }
}, { immediate: true })

onUnmounted(stopCarousel)
</script>

<style>
.app-root {
  min-height: 100vh;
  position: relative;
}
.cover-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  transition: opacity 0.4s ease, background-image 0.6s ease;
}
.app-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}
</style>
