import { ref } from 'vue'

const OPACITY_KEY = 'time-cyber-cover-opacity'
const COVER_IMAGES_KEY = 'time-cyber-cover-images'
const COVER_IMAGE_KEY = 'time-cyber-cover-image' // 兼容旧版单图
const COVER_CAROUSEL_KEY = 'time-cyber-cover-carousel'

const DEFAULT_POSITION = { x: 50, y: 50 }
const DEFAULT_SIZE = 100
const MAX_IMAGE_DIM = 1920
const JPEG_QUALITY = 0.82

function compressImage(dataUrl) {
  return new Promise((resolve) => {
    if (!dataUrl || !dataUrl.startsWith('data:image')) {
      resolve(dataUrl)
      return
    }
    const img = new Image()
    img.onload = () => {
      let w = img.width
      let h = img.height
      if (w <= MAX_IMAGE_DIM && h <= MAX_IMAGE_DIM) {
        resolve(dataUrl)
        return
      }
      const scale = MAX_IMAGE_DIM / Math.max(w, h)
      w = Math.round(w * scale)
      h = Math.round(h * scale)
      const canvas = document.createElement('canvas')
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        resolve(dataUrl)
        return
      }
      ctx.drawImage(img, 0, 0, w, h)
      try {
        const compressed = canvas.toDataURL('image/jpeg', JPEG_QUALITY)
        resolve(compressed)
      } catch {
        resolve(dataUrl)
      }
    }
    img.onerror = () => resolve(dataUrl)
    img.src = dataUrl
  })
}

function normalizeItem(item) {
  if (typeof item === 'string') {
    return { url: item, position: { ...DEFAULT_POSITION }, size: DEFAULT_SIZE }
  }
  return {
    url: item?.url || '',
    position: item?.position ? { x: Math.max(0, Math.min(100, Number(item.position.x) || 50)), y: Math.max(0, Math.min(100, Number(item.position.y) || 50)) } : { ...DEFAULT_POSITION },
    size: (item?.size >= 50 && item?.size <= 200) ? Number(item.size) : DEFAULT_SIZE
  }
}

function loadImages() {
  try {
    const s = localStorage.getItem(COVER_IMAGES_KEY)
    if (s) {
      const arr = JSON.parse(s)
      if (!Array.isArray(arr)) return []
      return arr.filter(Boolean).map(normalizeItem)
    }
    const legacy = localStorage.getItem(COVER_IMAGE_KEY)
    if (legacy) {
      localStorage.removeItem(COVER_IMAGE_KEY)
      return [normalizeItem(legacy)]
    }
  } catch (_) {}
  return []
}

function loadCarousel() {
  const n = Number(localStorage.getItem(COVER_CAROUSEL_KEY))
  return (n >= 0 && n <= 300) ? n : 0
}

const coverOpacity = ref(Number(localStorage.getItem(OPACITY_KEY)) || 0.6)
const coverImages = ref(loadImages())
const coverCarouselInterval = ref(loadCarousel())

export function useCover() {
  function setCoverOpacity(val) {
    const n = Math.max(0, Math.min(1, Number(val)))
    coverOpacity.value = n
    localStorage.setItem(OPACITY_KEY, String(n))
  }

  function saveImages() {
    try {
      if (coverImages.value.length) {
        localStorage.setItem(COVER_IMAGES_KEY, JSON.stringify(coverImages.value))
      } else {
        localStorage.removeItem(COVER_IMAGES_KEY)
      }
    } catch (e) {
      console.warn('图片过大，无法保存到本地', e)
    }
  }

  async function addCoverImage(dataUrl, position = DEFAULT_POSITION, size = DEFAULT_SIZE) {
    if (!dataUrl) return
    const compressed = await compressImage(dataUrl)
    coverImages.value = [...coverImages.value, normalizeItem({ url: compressed, position, size })]
    saveImages()
  }

  function updateCoverImageSettings(index, position, size) {
    const arr = [...coverImages.value]
    if (index < 0 || index >= arr.length) return
    arr[index] = {
      ...arr[index],
      position: { x: Math.max(0, Math.min(100, position.x)), y: Math.max(0, Math.min(100, position.y)) },
      size: Math.max(50, Math.min(200, Number(size) || DEFAULT_SIZE))
    }
    coverImages.value = arr
    saveImages()
  }

  function removeCoverImage(index) {
    coverImages.value = coverImages.value.filter((_, i) => i !== index)
    saveImages()
  }

  function setCoverImages(arr) {
    coverImages.value = (Array.isArray(arr) ? arr : []).filter(Boolean).map(normalizeItem)
    saveImages()
  }

  function setCoverCarouselInterval(seconds) {
    const n = Math.max(0, Math.min(300, Number(seconds)))
    coverCarouselInterval.value = n
    localStorage.setItem(COVER_CAROUSEL_KEY, String(n))
  }

  function clearCoverImages() {
    coverImages.value = []
    saveImages()
  }

  return {
    coverOpacity,
    coverImages,
    coverCarouselInterval,
    setCoverOpacity,
    addCoverImage,
    updateCoverImageSettings,
    removeCoverImage,
    setCoverImages,
    setCoverCarouselInterval,
    clearCoverImages
  }
}
