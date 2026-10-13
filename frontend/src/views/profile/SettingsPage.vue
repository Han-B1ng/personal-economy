<script setup lang="ts">
import { useWorkspace } from '@/hooks/useWorkspace'
import PageHeader from '@/components/common/PageHeader.vue'
import WorkspaceGate from '@/components/common/WorkspaceGate.vue'
const store = useWorkspace()
</script>
<template>
  <PageHeader title="账户设置" subtitle="Personal Economy · V1" back="/profile" /><WorkspaceGate
    ><div class="menu-row">
      <span>账户信息</span><span class="muted">{{ store.data?.profile.nickname }}</span>
    </div>
    <div class="menu-row"><span>修改密码</span><span class="muted">账号体系待接入</span></div>
    <div class="menu-row"><span>主题模式</span><span class="muted">深色</span></div>
    <RouterLink class="menu-row" to="/profile/data"
      ><span>数据管理</span><span class="muted">导出与管理 ›</span></RouterLink
    >
    <section class="detail-block">
      <h2>关于 Personal Economy</h2>
      <p class="muted">V1.0 · 本地验收版</p>
      <p class="info-note">个人成长的模拟行情系统。指数不代表人的实际价值，无证券交易功能。</p>
    </section>
    <p class="info-note">
      当前不是已登录账户。密码修改、退出登录与个人上市需等待身份流程确认后接入。
    </p></WorkspaceGate
  >
</template>
