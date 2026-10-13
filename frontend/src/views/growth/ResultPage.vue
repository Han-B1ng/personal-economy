<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import PageHeader from '@/components/common/PageHeader.vue'
import AppButton from '@/components/common/AppButton.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
const route = useRoute(),
  router = useRouter(),
  store = useWorkspace()
const kind = computed(() => String(route.meta.kind)),
  id = computed(() => String(route.params.id))
const noun = computed(
  () => ({ goals: '目标', tasks: '待办', events: '成长事件', finance: '财务记录' })[kind.value],
)
const exists = computed(() =>
  kind.value === 'finance'
    ? store.data?.financeRecords.some((v) => v.id === id.value)
    : kind.value === 'goals'
      ? store.data?.goals.some((v) => v.id === id.value && v.status === 'COMPLETED')
      : store.data?.[kind.value as 'tasks' | 'events'].some((v) => v.id === id.value),
)
</script>
<template>
  <PageHeader
    :title="`${noun}操作结果`"
    :back="kind === 'finance' ? '/finance' : `/growth/${kind}`"
  /><WorkspaceGate
    ><template v-if="exists"
      ><div class="result-mark">✓</div>
      <h2 class="result-heading">
        {{ kind === 'goals' ? '目标已完成' : kind === 'tasks' ? '待办状态已更新' : '记录已保存' }}
      </h2>
      <p class="result-description">
        {{
          kind === 'events'
            ? '成长事件已记录，指数评估尚待接入。'
            : kind === 'finance'
              ? '财务记录已保存，手动资产快照保持独立。'
              : '本次操作不会自动修改成长指数。'
        }}
      </p>
      <div class="button-stack">
        <AppButton v-if="kind === 'goals'" @click="router.push(`/growth/events/new?goal=${id}`)"
          >记录为成长事件</AppButton
        ><AppButton v-if="kind === 'events'" @click="router.push('/')">返回成长行情</AppButton
        ><AppButton
          variant="secondary"
          @click="router.push(kind === 'finance' ? '/finance/records' : `/growth/${kind}/${id}`)"
          >{{
            kind === 'goals' ? '仅完成目标' : kind === 'finance' ? '查看财务记录' : '查看详情'
          }}</AppButton
        >
      </div></template
    >
    <p v-else class="empty-note">记录不存在或已被删除。</p></WorkspaceGate
  >
</template>
