import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { router } from '@/router'
import '@fontsource-variable/inter-tight'
import '@fontsource-variable/noto-sans-sc'
import '@/assets/styles/global.css'
import '@/assets/styles/business.css'
import '@/assets/styles/motion.css'
import App from '@/App.vue'

createApp(App).use(createPinia()).use(router).mount('#app')
