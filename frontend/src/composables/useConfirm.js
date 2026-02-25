import { ref } from 'vue'

const confirmState = ref({ show: false, message: '', confirmText: '确认删除', resolve: null })

export function useConfirm() {
  function confirm(message, confirmText = '确认删除') {
    return new Promise((resolve) => {
      confirmState.value = { show: true, message, confirmText, resolve }
    })
  }
  function handleConfirm(ok) {
    confirmState.value.resolve?.(ok)
    confirmState.value = { show: false, message: '', confirmText: '确认删除', resolve: null }
  }
  return { confirmState, confirm, handleConfirm }
}
