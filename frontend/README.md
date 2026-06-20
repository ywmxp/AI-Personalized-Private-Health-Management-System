# 前端项目说明

## 技术栈

| 类别     | 选型                                           |
| -------- | ---------------------------------------------- |
| 框架     | Vue 3.5（Composition API +`<script setup>`） |
| 语言     | TypeScript 6                                   |
| 构建     | Vite 8                                         |
| UI 库    | Element Plus 2                                 |
| 路由     | Vue Router 5                                   |
| 状态管理 | Pinia                                          |
| HTTP     | Axios                                          |

---

## 目录结构

```
frontend/src/
│
├── main.ts              入口：注册 Pinia / Router / Element Plus，挂载 #app
├── App.vue               根组件，只放 <RouterView />
├── style.css             全局样式（颜色、字体、滚动条等）
├── env.d.ts              TS 声明，让 TS 认识 .vue 文件
│
├── views/                【页面】每个路由对应一个 .vue 文件
│   ├── AuthView.vue          登录 + 注册（不套后台壳）
│   ├── LayoutView.vue        后台布局壳（侧边栏 + 顶栏 + <RouterView />）
│   ├── HomeView.vue          主页 / 数据总览
│   ├── HealthDataView.vue    健康数据录入与查询
│   ├── AiAdviceView.vue      AI 健康分析
│   ├── ReminderView.vue      健康提醒
│   └── ProfileView.vue       个人中心
│
├── components/           【可复用组件】被多个 view 引用的零件
│   ├── layout/
│   │   ├── AppHeader.vue     顶部导航栏
│   │   └── AppSidebar.vue    侧边栏菜单
│   ├── health/
│   │   └── HealthChart.vue   健康数据图表
│   └── common/               ← 新写通用组件放这里
│       ├── LoadingSpinner.vue   加载中
│       └── EmptyState.vue       无数据占位
│
├── router/               【路由】URL ↔ 页面的映射
│   └── index.ts
│
├── api/                  【接口层】所有后端请求的封装
│   ├── request.ts            axios 实例：统一加 token、统一错误提示
│   ├── auth.ts               登录 / 注册 API
│   └── health.ts             健康数据 API
│
├── stores/               【全局状态】Pinia，跨页面共享的数据
│   └── user.ts              用户信息、token、登录/登出
│
├── types/                【类型定义】前后端共用的 TS 接口，所有人以此为准
│   ├── index.ts              统一导出
│   ├── api.ts                ApiResponse<T>
│   ├── user.ts               User / LoginRequest / LoginResponse 等
│   └── health.ts             HealthRecord / HealthDataUpload
│
├── utils/                【工具函数】纯函数，不依赖 Vue
│   └── index.ts              日期格式化等
│
└── assets/               【静态资源】图片、图标、字体等
```

---

## 路由设计

```
/login          →  AuthView.vue        （独立页面，无侧边栏/顶栏）
/home           →  LayoutView > HomeView
/health-data    →  LayoutView > HealthDataView
/ai-advice      →  LayoutView > AiAdviceView
/reminders      →  LayoutView > ReminderView
/profile        →  LayoutView > ProfileView
```

- 登录/注册是**独立页面**，不套后台壳
- 其余页面都嵌套在 `LayoutView` 内，自动带上侧边栏和顶栏
- 新增页面只需要在 `router/index.ts` 的 `children` 数组里加一条

---

## 数据流

```
View（页面）
  ↓ 调用
api/xxx.ts（接口函数）
  ↓ 使用
api/request.ts（axios 实例 → 自动带 token → 统一错误处理）
  ↓ 发送
后端 API
  ↓ 返回
View 拿到数据 → 更新页面 / 写入 store
```

**规则：**

- View 里**不要**直接 `import axios`，必须通过 `api/` 里的函数发请求
- 跨页面共享的数据（如用户信息）存到 `stores/`，页面私有数据用 `ref`/`reactive`
- 接口的请求和响应类型在 `types/` 里定义，`api/` 和 `stores/` 都引用它

---

## 开发约定

### 新增一个页面

1. 在 `views/` 下进行填写文件，或新建 `XxxView.vue`进行开发
2. 在 `router/index.ts` 的 `children` 数组加一条路由
3. 如果需要新接口，在 `api/` 下新建 `xxx.ts`
4. 如果需要新类型，在 `types/` 下新建 `xxx.ts`

### 新增一个可复用组件

- 被多个 view 用到的 → `components/common/`
- 只在特定业务里复用的 → `components/<业务名>/`（如 `health/`）

### 新增全局状态

- 在 `stores/` 下新建文件，用 `defineStore` 定义
- 只在确实需要跨页面共享时才用 store，不要全往里塞

### 样式

- 全局样式写 `style.css`
- 页面/组件样式用 `<style scoped>`，避免互相污染

---

## 本地开发

```bash
cd frontend
npm install
npm run dev
```

默认运行在 `http://127.0.0.1:5173`，API 请求自动代理到后端 `http://127.0.0.1:8080`（配置在 `vite.config.ts`）。
