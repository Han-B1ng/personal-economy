<script setup lang="ts">
import { computed, onBeforeUnmount, ref, useId, watch } from 'vue'
import { chartGeometry, formatIndex, nearestPoint, shortDate } from '@/utils/chart'
import AppIcon from '@/components/common/AppIcon.vue'
import type { IndexPoint } from '@/types/home'
const props = defineProps<{ points: IndexPoint[] }>()
const selected = ref<number | null>(null)
const dragging = ref(false)
const surface = ref<HTMLElement>()
const svg = ref<SVGSVGElement>()
const gradientId = `chart-fill-${useId()}`
const geometry = computed(() => chartGeometry(props.points))
const selectedPoint = computed(() =>
  selected.value === null ? null : props.points[selected.value],
)
const selectedPosition = computed(() =>
  selected.value === null ? null : geometry.value.coordinates[selected.value],
)
const latest = computed(() => geometry.value.coordinates.at(-1))
const falling = computed(() => (props.points.at(-1)?.value || 0) < (props.points[0]?.value || 0))
const dates = computed(() => {
  const first = props.points[0],
    last = props.points.at(-1)
  return first && last
    ? [
        shortDate(first.timestamp),
        shortDate((first.timestamp + last.timestamp) / 2),
        shortDate(last.timestamp),
      ]
    : []
})
let frame: number | undefined
let pending: { x: number; y: number } | undefined
function cancelFrame() {
  if (frame !== undefined) window.cancelAnimationFrame(frame)
  frame = undefined
  pending = undefined
}
function applyPointer(x: number, y: number) {
  if (!props.points.length || !svg.value) return
  const matrix = svg.value.getScreenCTM?.()
  let ratio: number
  if (matrix && typeof DOMPoint !== 'undefined') {
    ratio = new DOMPoint(x, y).matrixTransform(matrix.inverse()).x / 291
  } else {
    const bounds = surface.value?.getBoundingClientRect()
    if (!bounds?.width) return
    ratio = ((x - bounds.left) / bounds.width) * (345 / 291)
  }
  const next = nearestPoint(props.points, ratio)
  if (next !== selected.value) selected.value = next
}
function movePointer(event: PointerEvent) {
  if (!dragging.value) return
  pending = { x: event.clientX, y: event.clientY }
  if (frame !== undefined) return
  frame = window.requestAnimationFrame(() => {
    frame = undefined
    const point = pending
    pending = undefined
    if (point && dragging.value) applyPointer(point.x, point.y)
  })
}
function startPointer(event: PointerEvent) {
  if (event.isPrimary === false || event.button !== 0) return
  dragging.value = true
  try {
    ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
  } catch {
    /* Pointer may have ended during a render. */
  }
  applyPointer(event.clientX, event.clientY)
}
function stopPointer(event: PointerEvent) {
  if (dragging.value) applyPointer(event.clientX, event.clientY)
  dragging.value = false
  cancelFrame()
}
function cancelPointer() {
  dragging.value = false
  cancelFrame()
}
function clearSelection() {
  selected.value = null
  surface.value?.focus({ preventScroll: true })
}
watch(
  () => props.points,
  () => {
    selected.value = null
    cancelPointer()
  },
)
onBeforeUnmount(cancelFrame)
function selectKey(event: KeyboardEvent) {
  const last = props.points.length - 1
  if (last < 0) return
  switch (event.key) {
    case 'ArrowLeft':
      selected.value = Math.max(0, (selected.value ?? last) - 1)
      break
    case 'ArrowRight':
      selected.value = Math.min(last, (selected.value ?? -1) + 1)
      break
    case 'Home':
      selected.value = 0
      break
    case 'End':
      selected.value = last
      break
    case 'Escape':
      selected.value = null
      break
    default:
      return
  }
  event.preventDefault()
}
</script>

<template>
  <div class="growth-chart" :class="{ falling, inspecting: selectedPoint }">
    <div class="chart-readout">
      <template v-if="selectedPoint">
        <span class="readout-date">{{ shortDate(selectedPoint.timestamp) }} · 演示指数</span>
        <strong>{{ formatIndex(selectedPoint.value) }}</strong>
        <button
          type="button"
          class="clear-selection"
          aria-label="取消行情节点选择"
          @click="clearSelection"
        >
          <AppIcon name="close" :size="14" />
        </button>
      </template>
      <template v-else
        ><span>按住或用方向键查看历史</span
        ><span class="latest-date">{{ dates[2] }}</span></template
      >
    </div>
    <div
      ref="surface"
      class="chart-interaction"
      role="slider"
      tabindex="0"
      aria-label="成长指数走势图，左右方向键查看历史，Escape取消选择"
      :aria-valuemin="0"
      :aria-valuemax="Math.max(0, points.length - 1)"
      :aria-valuenow="selected ?? Math.max(0, points.length - 1)"
      :aria-valuetext="
        selectedPoint
          ? `${shortDate(selectedPoint.timestamp)}，${formatIndex(selectedPoint.value)}点，演示数据`
          : '最新行情，演示数据'
      "
      @keydown="selectKey"
      @pointerdown="startPointer"
      @pointermove="movePointer"
      @pointerup="stopPointer"
      @pointercancel="cancelPointer"
      @lostpointercapture="cancelPointer"
    >
      <svg ref="svg" viewBox="0 0 345 198" aria-hidden="true">
        <defs>
          <linearGradient :id="gradientId" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="var(--chart-color)" stop-opacity="0.11" />
            <stop offset="100%" stop-color="var(--chart-color)" stop-opacity="0" />
          </linearGradient>
        </defs>
        <g v-for="tick in geometry.ticks" :key="tick.y">
          <path :d="`M0 ${tick.y}H291`" class="grid-line" />
          <text x="321" :y="tick.y + 3" text-anchor="middle" class="axis">
            {{ Math.round(tick.value).toLocaleString('en-US') }}
          </text>
        </g>
        <Transition name="plot">
          <g :key="geometry.line">
            <path v-if="geometry.area" :d="geometry.area" :fill="`url(#${gradientId})`" />
            <path v-if="latest" :d="`M0 ${latest.y}H291`" class="reference-line" />
            <path :d="geometry.line" class="chart-line" vector-effect="non-scaling-stroke" />
            <circle v-if="latest" :cx="latest.x" :cy="latest.y" r="3" class="chart-dot" />
          </g>
        </Transition>
        <g v-if="selectedPosition">
          <path :d="`M${selectedPosition.x} 8V174`" class="crosshair" />
          <circle :cx="selectedPosition.x" :cy="selectedPosition.y" r="5" class="selected-dot" />
        </g>
        <text x="0" y="190" class="axis">{{ dates[0] }}</text>
        <text x="145.5" y="190" text-anchor="middle" class="axis">{{ dates[1] }}</text>
        <text x="291" y="190" text-anchor="end" class="axis">{{ dates[2] }}</text>
      </svg>
    </div>
  </div>
</template>

<style scoped>
.growth-chart {
  --chart-color: var(--color-positive);
  position: relative;
}
.growth-chart.falling {
  --chart-color: var(--color-error);
}
.chart-readout {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 32px;
  color: var(--color-muted);
  font-size: 10px;
}
.chart-readout strong {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-left: auto;
  font-variant-numeric: tabular-nums;
}
.latest-date {
  margin-left: auto;
}
.clear-selection {
  display: grid;
  place-items: center;
  min-width: 44px;
  min-height: 44px;
  margin-right: -8px;
  color: var(--color-muted);
  border: 0;
  background: transparent;
  border-radius: 8px;
}
.clear-selection:active {
  color: var(--color-text);
  background: var(--color-surface);
}
.chart-interaction {
  cursor: crosshair;
  touch-action: pan-y;
  border-radius: 4px;
}
svg {
  display: block;
  width: 100%;
  overflow: visible;
}
.axis {
  font-family: var(--font-body);
  font-size: 10px;
  fill: var(--color-muted);
}
.grid-line {
  stroke: var(--color-border);
  stroke-width: 0.7;
  stroke-dasharray: 2 5;
}
.chart-line {
  fill: none;
  stroke: var(--chart-color);
  stroke-width: 1.8;
  stroke-linejoin: round;
  stroke-linecap: round;
}
.reference-line {
  stroke: var(--chart-color);
  stroke-opacity: 0.28;
  stroke-width: 0.7;
  stroke-dasharray: 5 6;
}
.chart-dot {
  fill: var(--chart-color);
}
.crosshair {
  stroke: var(--color-muted);
  stroke-width: 0.8;
  stroke-dasharray: 3 4;
}
.selected-dot {
  fill: var(--chart-color);
  stroke: var(--color-background);
  stroke-width: 2;
}
.plot-enter-active,
.plot-leave-active {
  transition: opacity var(--motion-standard) var(--ease-standard);
}
.plot-enter-from,
.plot-leave-to {
  opacity: 0;
}
</style>
