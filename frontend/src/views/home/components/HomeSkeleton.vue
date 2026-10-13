<template>
  <section class="home-skeleton" aria-busy="true" aria-label="正在读取行情">
    <span class="sr-only">正在读取个人成长行情</span>
    <div class="skeleton-label skeleton" />
    <div class="skeleton-index skeleton" />
    <div class="skeleton-change skeleton" />
    <div class="skeleton-chart skeleton" />
    <div class="skeleton-periods"><span v-for="item in 6" :key="item" class="skeleton" /></div>
    <div class="skeleton-heading skeleton" />
    <div v-for="item in 3" :key="item" class="skeleton-row">
      <span class="skeleton-category skeleton" />
      <div><span class="skeleton-title skeleton" /><span class="skeleton-date skeleton" /></div>
      <span class="skeleton-score skeleton" />
    </div>
  </section>
</template>
<style scoped>
.home-skeleton {
  padding-top: 28px;
}
.skeleton {
  display: block;
  position: relative;
  overflow: hidden;
  border-radius: 5px;
  background: var(--color-surface-raised);
}
.skeleton::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, #ffffff06, transparent);
  transform: translateX(-100%);
  animation: skeleton-pass 1.4s ease-in-out 3;
}
.skeleton-label {
  width: 88px;
  height: 14px;
  margin-bottom: 12px;
}
.skeleton-index {
  width: 200px;
  height: 52px;
}
.skeleton-change {
  width: 150px;
  height: 18px;
  margin-top: 8px;
}
.skeleton-chart {
  height: 226px;
  margin-top: 28px;
  background: var(--color-surface);
}
.skeleton-periods {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  padding-block: 16px;
}
.skeleton-periods span {
  height: 20px;
}
.skeleton-heading {
  width: 114px;
  height: 18px;
  margin: 24px 0 12px;
}
.skeleton-row {
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.skeleton-category {
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
}
.skeleton-row > div {
  flex: 1;
}
.skeleton-title {
  width: 80%;
  height: 13px;
}
.skeleton-date {
  width: 64px;
  height: 10px;
  margin-top: 8px;
}
.skeleton-score {
  width: 48px;
  height: 16px;
}
@keyframes skeleton-pass {
  to {
    transform: translateX(100%);
  }
}
</style>
