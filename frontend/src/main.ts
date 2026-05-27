import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
// 1. 引入Element Plus和它的样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 2. 引入路由（后面我们会创建这个文件）
import router from './router'

// 3. 创建应用实例
const app = createApp(App)

// 4. 全局注册Element Plus（所有页面都能直接用它的组件）
app.use(ElementPlus)
// 5. 全局注册路由（实现页面跳转）
app.use(router)

// 6. 挂载应用到页面
app.mount('#app')
