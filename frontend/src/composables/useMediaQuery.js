import { onMounted, onScopeDispose, ref } from 'vue'

// Keep layout props in sync when a device rotates or the browser is resized.
export function useMediaQuery(query) {
  const media = window.matchMedia(query)
  const matches = ref(media.matches)
  const update = (event) => { matches.value = event.matches }

  onMounted(() => {
    matches.value = media.matches
    media.addEventListener('change', update)
  })
  onScopeDispose(() => media.removeEventListener('change', update))

  return matches
}
