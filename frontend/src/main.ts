import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 每次启动时清除登录状态，确保初始化为未登录状态
localStorage.removeItem('user_isLoggedIn')
localStorage.removeItem('user_isAdmin')
localStorage.removeItem('admin_isLoggedIn')

app.use(router)
app.use(ElementPlus)
app.mount('#app')
