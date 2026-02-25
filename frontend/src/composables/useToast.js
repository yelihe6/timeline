import { ref } from 'vue'

const toast = ref({ show: false, message: '' })
let timer = null

export function useToast() {
  function show(message) {
    if (timer) clearTimeout(timer)
    toast.value = { show: true, message }
    timer = setTimeout(() => {
      toast.value = { show: false, message: '' }
      timer = null
    }, 2500)
  }
  return { toast, show }
}
