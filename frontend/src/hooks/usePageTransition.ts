import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

/** Primary tabs fade; deeper pages follow history direction. No timer gates navigation. */
export function usePageTransition() {
  const route = useRoute()
  const transition = ref('page-fade')
  let previousPosition = Number(window.history.state?.position || 0)
  watch(
    () => route.path,
    (_path, previousPath) => {
      const position = Number(window.history.state?.position || 0)
      const previousDepth = previousPath.split('/').filter(Boolean).length
      const nextDepth = route.path.split('/').filter(Boolean).length
      transition.value =
        route.meta.hideNavigation || previousDepth > 2
          ? position < previousPosition || nextDepth < previousDepth
            ? 'page-back'
            : 'page-forward'
          : 'page-fade'
      previousPosition = position
    },
    { flush: 'pre' },
  )
  function disableLeavingPage(element: Element) {
    const page = element as HTMLElement
    page.inert = true
    page.setAttribute('aria-hidden', 'true')
  }
  function enableEnteringPage(element: Element) {
    const page = element as HTMLElement
    page.inert = false
    page.removeAttribute('aria-hidden')
  }
  return { transition, disableLeavingPage, enableEnteringPage }
}
