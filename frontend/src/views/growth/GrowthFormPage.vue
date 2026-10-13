<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWorkspace } from '@/hooks/useWorkspace'
import { today } from '@/utils/display'
import PageHeader from '@/components/common/PageHeader.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
import AppButton from '@/components/common/AppButton.vue'
import SegmentedControl from '@/components/common/SegmentedControl.vue'
const route = useRoute(),
  router = useRouter(),
  store = useWorkspace(),
  error = ref(''),
  newCategory = ref(''),
  addingCategory = ref(false),
  errorElement = ref<HTMLElement>()
let mounted = true
onBeforeUnmount(() => {
  mounted = false
})
const kind = computed(() => String(route.meta.kind)),
  id = computed(() => String(route.params.id || '')),
  editing = computed(() => !!id.value)
const nouns: Record<string, string> = { goals: '目标', tasks: '待办', events: '成长事件' }
const form = reactive({
  title: '',
  description: '',
  categoryId: 'cat-0',
  startDate: today(),
  dueDate: today(),
  dueTime: '18:00',
  date: today(),
  progress: 0,
  priority: '普通',
  importance: '普通',
  goalId: '',
  sourceGoalId: '',
})
let initialized = ''
const categories = computed(() => store.data?.categories.filter((c) => c.active) || [])
const inactiveCategory = computed(() =>
  store.data?.categories.find((c) => c.id === form.categoryId && !c.active),
)
async function showError(message: string) {
  error.value = message
  await nextTick()
  if (mounted) errorElement.value?.focus()
}
const existing = computed(() =>
  editing.value
    ? (store.data?.[kind.value as 'goals' | 'tasks' | 'events'] || []).find(
        (v) => v.id === id.value,
      )
    : null,
)
watch(
  [() => route.fullPath, () => store.data],
  () => {
    if (!store.data || initialized === route.fullPath) return
    initialized = route.fullPath
    Object.assign(
      form,
      {
        title: '',
        description: '',
        categoryId: categories.value[0]?.id || '',
        startDate: today(),
        dueDate: today(),
        dueTime: '18:00',
        date: today(),
        progress: 0,
        priority: '普通',
        importance: '普通',
        goalId: String(route.query.goal || ''),
        sourceGoalId: String(route.query.goal || ''),
      },
      existing.value || {},
    )
    error.value = ''
  },
  { immediate: true },
)
async function addCategory() {
  if (store.saving) return
  if (!newCategory.value.trim()) {
    await showError('请输入分类名称')
    return
  }
  error.value = ''
  try {
    const result = await store.save('/categories', 'POST', { name: newCategory.value.trim() })
    form.categoryId = result || ''
    newCategory.value = ''
    addingCategory.value = false
  } catch (e) {
    await showError(e instanceof Error ? e.message : '分类保存失败')
  }
}
async function submit() {
  if (store.saving) return
  const originPath = route.fullPath
  error.value = ''
  if (!form.title.trim()) {
    await showError('请输入名称')
    return
  }
  if (kind.value !== 'tasks' && !categories.value.some((c) => c.id === form.categoryId)) {
    await showError('分类已停用或不可用，请选择其他分类')
    return
  }
  if (kind.value === 'goals' && form.dueDate < form.startDate) {
    await showError('截止日期不能早于开始日期')
    return
  }
  if (
    kind.value === 'goals' &&
    editing.value &&
    (!Number.isInteger(Number(form.progress)) ||
      Number(form.progress) < 0 ||
      Number(form.progress) > 100)
  ) {
    await showError('手动进度请输入0至100的整数')
    return
  }
  const linkedGoal =
    kind.value === 'tasks' ? form.goalId : kind.value === 'events' ? form.sourceGoalId : ''
  if (linkedGoal && !store.data?.goals.some((g) => g.id === linkedGoal)) {
    await showError('关联目标不可用，请重新选择或从有效目标进入')
    return
  }
  try {
    const body =
      kind.value === 'goals'
        ? {
            title: form.title,
            description: form.description,
            categoryId: form.categoryId,
            startDate: form.startDate,
            dueDate: form.dueDate,
            progress: editing.value ? Number(form.progress) : 0,
          }
        : kind.value === 'tasks'
          ? {
              title: form.title,
              description: form.description,
              dueDate: form.dueDate,
              dueTime: form.dueTime,
              priority: form.priority,
              goalId: form.goalId || null,
            }
          : {
              title: form.title,
              description: form.description,
              categoryId: form.categoryId,
              date: form.date,
              importance: form.importance,
              sourceGoalId: form.sourceGoalId || null,
            }
    const saved = await store.save(
      `/${kind.value}${editing.value ? '/' + id.value : ''}`,
      editing.value ? 'PUT' : 'POST',
      body,
    )
    if (!saved) throw new Error('保存响应缺少记录标识，请刷新列表确认结果')
    if (mounted && route.fullPath === originPath)
      await router.push(`/growth/${kind.value}/${saved}${kind.value === 'events' ? '/saved' : ''}`)
  } catch (e) {
    await showError(e instanceof Error ? e.message : '保存失败，请重试')
  }
}
</script>
<template>
  <PageHeader
    :title="`${editing ? '编辑' : '新增'}${nouns[kind]}`"
    :back="`/growth/${kind}`"
    :subtitle="
      kind === 'tasks'
        ? '具体行动，不直接影响成长指数。'
        : kind === 'events'
          ? '记录成长事实，指数评估尚待接入。'
          : '目标进度由你手动维护。'
    "
  />
  <WorkspaceGate
    ><p v-if="editing && !existing" class="empty-note">记录不存在或已被删除。</p>
    <form
      v-else
      class="form-layout"
      :aria-busy="store.saving || undefined"
      @submit.prevent="submit"
    >
      <fieldset class="growth-fields" :disabled="store.saving">
        <label class="field-label"
          >{{ nouns[kind] }}名称<input
            v-model.trim="form.title"
            required
            maxlength="120"
            placeholder="输入名称"
        /></label>
        <label v-if="kind !== 'tasks'" class="field-label"
          >{{ kind === 'goals' ? '目标' : '事件' }}分类<select v-model="form.categoryId" required>
            <option v-if="inactiveCategory" :value="inactiveCategory.id" disabled>
              {{ inactiveCategory.name }} · 已停用，请重新选择
            </option>
            <option v-for="item in categories" :key="item.id" :value="item.id">
              {{ item.name }}{{ kind === 'goals' ? '成长' : '' }}
            </option>
          </select></label
        >
        <template v-if="kind === 'events'"
          ><button
            type="button"
            class="text-link filter-chip"
            @click="addingCategory = !addingCategory"
          >
            ＋ 自定义分类
          </button>
          <div v-if="addingCategory" class="form-field">
            <label
              >分类名称<input
                v-model.trim="newCategory"
                maxlength="24"
                @keydown.enter.prevent="addCategory" /></label
            ><AppButton
              type="button"
              variant="secondary"
              :loading="store.saving"
              @click="addCategory"
              >添加分类</AppButton
            >
          </div></template
        >
        <label class="field-label"
          >描述<textarea
            v-model="form.description"
            :maxlength="kind === 'events' ? 4000 : 2000"
            placeholder="记录过程、成果与值得记住的细节"
          />
        </label>
        <div v-if="kind === 'goals'" class="field-pair">
          <label class="field-label"
            >计划开始日期<input v-model="form.startDate" type="date" required /></label
          ><label class="field-label"
            >目标截止日期<input v-model="form.dueDate" type="date" :min="form.startDate" required
          /></label>
        </div>
        <label v-if="kind === 'goals' && editing" class="field-label"
          >手动进度（%）<input
            v-model.number="form.progress"
            type="number"
            min="0"
            max="100"
            step="1"
            required
        /></label>
        <div v-if="kind === 'tasks'" class="field-pair">
          <label class="field-label"
            >截止日期<input v-model="form.dueDate" type="date" required /></label
          ><label class="field-label"
            >截止时间<input v-model="form.dueTime" type="time" required
          /></label>
        </div>
        <label v-if="kind === 'events'" class="field-label"
          >发生日期<input v-model="form.date" type="date" required
        /></label>
        <div v-if="kind === 'tasks'">
          <label class="field-label">待办优先级</label
          ><SegmentedControl
            v-model="form.priority"
            :options="['普通', '重要', '紧急']"
            label="待办优先级"
          />
        </div>
        <div v-if="kind === 'events'">
          <label class="field-label">重要程度（用户参考）</label
          ><SegmentedControl
            v-model="form.importance"
            :options="['普通', '重要', '重大']"
            label="事件重要程度"
          />
          <p class="info-note">用户选择不作为最终权重；新事件保存为待评估，不自动更新指数。</p>
        </div>
        <label v-if="kind === 'tasks'" class="field-label"
          >关联目标（可选）<select v-model="form.goalId">
            <option value="">不关联目标</option>
            <option v-for="goal in store.data?.goals" :key="goal.id" :value="goal.id">
              {{ goal.title }}
            </option>
          </select></label
        >
        <p v-if="kind === 'events' && form.sourceGoalId" class="info-note">
          来自目标：{{ store.data?.goals.find((g) => g.id === form.sourceGoalId)?.title }}
        </p>
      </fieldset>
      <p v-if="error" ref="errorElement" class="form-error" role="alert" tabindex="-1">
        {{ error }}。输入内容已保留；若请求超时，请先检查列表是否已经保存再重试。
      </p>
      <AppButton type="submit" :loading="store.saving">{{
        store.saving ? '保存中…' : `保存${nouns[kind]}`
      }}</AppButton>
    </form></WorkspaceGate
  >
</template>

<style scoped>
.growth-fields {
  display: contents;
  border: 0;
  margin: 0;
  padding: 0;
  min-inline-size: 0;
}
.growth-fields:disabled :deep(input),
.growth-fields:disabled :deep(textarea),
.growth-fields:disabled :deep(select) {
  opacity: 0.65;
  cursor: progress;
}
.form-error:focus {
  outline: 1px solid var(--color-error);
  outline-offset: 6px;
  border-radius: 2px;
}
</style>
