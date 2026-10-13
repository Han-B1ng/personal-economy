import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppButton from '@/components/common/AppButton.vue'
import FormInput from '@/components/common/FormInput.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'

describe('shared controls', () => {
  it('prevents loading buttons from submitting twice', () => {
    const wrapper = mount(AppButton, { props: { loading: true, type: 'submit' } })
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
    expect(wrapper.get('button').attributes('aria-busy')).toBe('true')
  })
  it('exposes validation errors to the associated input', async () => {
    const wrapper = mount(FormInput, {
      props: { modelValue: '', label: '目标名称', error: '请输入目标名称' },
    })
    const input = wrapper.get('input')
    expect(wrapper.get('label').attributes('for')).toBe(input.attributes('id'))
    expect(input.attributes('aria-describedby')).toBe(wrapper.get('p').attributes('id'))
    await input.setValue('英语学习')
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['英语学习'])
  })
  it('supports keyboard navigation and wraparound in segmented choices', async () => {
    const wrapper = mount(SegmentedControl, {
      props: { modelValue: '1M', options: ['1D', '1W', '1M'], label: '周期' },
    })
    const selected = wrapper.findAll('button')[2]!
    expect(selected.attributes('tabindex')).toBe('0')
    await selected.trigger('keydown', { key: 'ArrowRight' })
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['1D'])
  })
})
