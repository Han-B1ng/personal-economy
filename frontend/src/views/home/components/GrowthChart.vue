<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { chartGeometry, formatIndex, nearestPoint, shortDate } from '@/utils/chart'
import type { IndexPoint } from '@/types/home'

const props = defineProps<{ points: IndexPoint[] }>()
const selected = ref<number | null>(null)
const dragging = ref(false)
const geometry = computed(() => chartGeometry(props.points))
const selectedPoint = computed(() =>
  selected.value === null ? null : props.points[selected.value],
)
const selectedPosition = computed(() =>
  selected.value === null ? null : geometry.value.coordinates[selected.value],
)
const latest = computed(() => geometry.value.coordinates.at(-1))
const dates = computed(() => {
  const first = props.points[0]
  const last = props.points.at(-1)
  return first && last
    ? [
        shortDate(first.timestamp),
        shortDate((first.timestamp + last.timestamp) / 2),
        shortDate(last.timestamp),
      ]
    : []
})
watch(
  () => props.points,
  () => {
    selected.value = null
  },
)
function selectPointer(event: PointerEvent) {
  if (!dragging.value && event.type === 'pointermove') return
  const bounds = (event.currentTarget as HTMLElement).getBoundingClientRect()
  selected.value = nearestPoint(
    props.points,
    (((event.clientX - bounds.left) / bounds.width) * 345) / 291,
  )
}
function startPointer(event: PointerEvent) {
  dragging.value = true
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
  selectPointer(event)
}
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
  <div class="growth-chart">
    <div v-if="selectedPoint" class="chart-tooltip" role="status">
      <span>{{ shortDate(selectedPoint.timestamp) }} · 演示指数</span>
      <strong>{{ formatIndex(selectedPoint.value) }}</strong>
      <button aria-label="取消行情节点选择" @click="selected = null">×</button>
    </div>
    <div
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
      @pointermove="selectPointer"
      @pointerup="dragging = false"
      @pointercancel="dragging = false"
    >
      <svg viewBox="0 0 345 214" aria-hidden="true">
        <g v-for="tick in geometry.ticks" :key="tick.y">
          <path :d="`M0 ${tick.y}H291`" class="grid-line" />
          <text x="321" :y="tick.y + 3" text-anchor="middle" class="axis">
            {{ Math.round(tick.value).toLocaleString('en-US') }}
          </text>
        </g>
        <path v-if="geometry.area" :d="geometry.area" class="chart-area" />
        <path v-if="latest" :d="`M0 ${latest.y}H291`" class="reference-line" />
        <path :d="geometry.line" class="chart-line" />
        <circle v-if="latest" :cx="latest.x" :cy="latest.y" r="3" class="chart-dot" />
        <g v-if="selectedPosition">
          <path :d="`M${selectedPosition.x} 0V174`" class="crosshair" />
          <circle :cx="selectedPosition.x" :cy="selectedPosition.y" r="5" class="selected-dot" />
        </g>
        <text x="0" y="198" class="axis">{{ dates[0] }}</text>
        <text x="145.5" y="198" text-anchor="middle" class="axis">{{ dates[1] }}</text>
        <text x="291" y="198" text-anchor="end" class="axis">{{ dates[2] }}</text>
      </svg>
    </div>
  </div>
</template>

<style scoped>
.growth-chart {
  position: relative;
}
.chart-interaction {
  cursor: crosshair;
  touch-action: pan-y;
}
svg {
  display: block;
  width: 100%;
  overflow: visible;
}
.axis {
  font-family: var(--font-body);
  font-size: 10px;
  fill: var(--color-subtle);
}
.grid-line {
  stroke: var(--color-border);
  stroke-width: 0.7;
}
.chart-area {
  fill: var(--color-positive);
  fill-opacity: 0.05;
}
.chart-line {
  fill: none;
  stroke: var(--color-positive);
  stroke-width: 1.8;
  stroke-linejoin: round;
  stroke-linecap: round;
}
.reference-line {
  stroke: var(--color-positive);
  stroke-opacity: 0.32;
  stroke-width: 0.7;
  stroke-dasharray: 10 11;
}
.chart-dot {
  fill: var(--color-positive);
}
.crosshair {
  stroke: var(--color-muted);
  stroke-width: 0.8;
  stroke-dasharray: 3 4;
}
.selected-dot {
  fill: var(--color-positive);
  stroke: var(--color-background);
  stroke-width: 2;
}
.chart-tooltip {
  position: absolute;
  left: 0;
  top: -12px;
  z-index: 1;
  display: grid;
  gap: 4px;
  min-width: 164px;
  padding: 8px 40px 8px 12px;
  background: var(--color-surface-raised);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  pointer-events: none;
}
.chart-tooltip span {
  font-size: 10px;
  color: var(--color-muted);
}
.chart-tooltip strong {
  font-size: 15px;
  font-weight: 500;
}
.chart-tooltip button {
  position: absolute;
  right: 0;
  top: 0;
  width: 40px;
  min-height: 44px;
  background: none;
  border: 0;
  color: var(--color-muted);
  font-size: 20px;
  pointer-events: auto;
}
</style>
