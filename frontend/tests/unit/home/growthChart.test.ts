import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import GrowthChart from '@/views/home/components/GrowthChart.vue'
const points = [
  { timestamp: Date.UTC(2026, 9, 1), value: 1000 },
  { timestamp: Date.UTC(2026, 9, 2), value: 1012 },
]
afterEach(() => {
  vi.restoreAllMocks()
  document.body.innerHTML = ''
})
describe('chart inspection', () => {
  it('keeps keyboard selection within its range and clears it on a period change', async () => {
    const wrapper = mount(GrowthChart, { props: { points } })
    const slider = wrapper.get('[role="slider"]')
    await slider.trigger('keydown', { key: 'Home' })
    await slider.trigger('keydown', { key: 'ArrowLeft' })
    expect(slider.attributes('aria-valuenow')).toBe('0')
    expect(wrapper.get('.chart-readout strong').text()).toBe('1,000.00')
    await wrapper.setProps({ points: [points[1]!] })
    expect(wrapper.find('.chart-readout strong').exists()).toBe(false)
    expect(slider.attributes('aria-valuemax')).toBe('0')
    wrapper.unmount()
  })
  it('returns focus to the chart when closing an inspection', async () => {
    const wrapper = mount(GrowthChart, { props: { points }, attachTo: document.body })
    const slider = wrapper.get('[role="slider"]')
    await slider.trigger('keydown', { key: 'End' })
    await wrapper.get('.clear-selection').trigger('click')
    expect(document.activeElement).toBe(slider.element)
    expect(wrapper.find('.clear-selection').exists()).toBe(false)
    wrapper.unmount()
  })
  it('cancels a queued drag update when the chart unmounts', async () => {
    const raf = vi.spyOn(window, 'requestAnimationFrame').mockReturnValue(27)
    const cancel = vi.spyOn(window, 'cancelAnimationFrame').mockImplementation(() => {})
    const wrapper = mount(GrowthChart, { props: { points } })
    const slider = wrapper.get('[role="slider"]')
    vi.spyOn(slider.element, 'getBoundingClientRect').mockReturnValue({
      left: 0,
      width: 345,
    } as DOMRect)
    slider.element.dispatchEvent(
      new window.MouseEvent('pointerdown', { button: 0, clientX: 20, clientY: 50 }),
    )
    slider.element.dispatchEvent(
      new window.MouseEvent('pointermove', { clientX: 100, clientY: 50 }),
    )
    expect(raf).toHaveBeenCalledOnce()
    wrapper.unmount()
    expect(cancel).toHaveBeenCalledWith(27)
  })
})
