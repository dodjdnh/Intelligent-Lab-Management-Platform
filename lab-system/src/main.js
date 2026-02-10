import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'

// 【关键步骤】引入你刚才在 router/index.js 中配置的路由
import router from './router' 

const app = createApp(App)

// 注册所有图标（保持不变，供菜单使用）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 【关键步骤】使用 .use(router) 插件，使路由功能在全局生效
app.use(router) 

app.use(ElementPlus)
app.mount('#app')