# AI个性化私人健康管理系统

一个面向日常健康管理场景的全栈项目，包含用户认证、健康数据记录与导入、趋势展示、AI 健康画像、AI 健康计划、健康知识推送、提醒管理和管理员统计能力。

当前仓库不是“空骨架”，而是已经具备前后端页面、核心接口、数据库初始化脚本和 AI 服务接入链路的可运行开发版本。

## 当前实现状态

已接入并可运行的模块：

- 用户注册、登录、JWT 鉴权
- 个人信息查看与修改、密码修改
- 健康数据新增、分页查询、删除
- 健康数据 CSV 批量导入
- 健康趋势查询
- AI 健康画像生成
- AI 健康计划生成
- AI 健康知识推送与已读标记
- 健康提醒增删改查与启停
- 管理员用户列表、状态切换、登录日志、平台统计
- Vue 3 前端页面与路由守卫
- FastAPI AI 服务与 Spring Boot 代理调用

当前仍需注意的点：

- 管理员相关页面和接口已存在，但仓库内没有默认管理员初始化数据
- AI 服务未配置 `AI_API_KEY` 或未启动时，Spring Boot 侧会回退到内置 mock 结果，便于联调
- 前端仍保留 `VITE_MOCK_API` / `VITE_MOCK_AUTH` 开关，便于纯前端开发
- 仓库内部分设计文档仍是草案，和代码实现可能不完全同步

## 技术栈

- 前端：Vue 3.5.x、TypeScript、Vite 8.x、Pinia、Vue Router 5.x、Element Plus
- 主后端：Spring Boot 3.5.7、Java 21、Spring Security、Spring Data JPA、MySQL
- AI 服务：FastAPI 0.115+、Python 3.10+、AutoGen/OpenAI Compatible API
- 数据库：MySQL 8.4（Docker Compose）

## 目录结构

```text
.
├── frontend/                  # Vue 3 前端
├── backend/                   # Spring Boot 后端 + FastAPI AI 服务
│   ├── src/main/java/         # Java API
│   ├── src/main/python/       # FastAPI AI 服务
│   └── src/test/              # Java / Python 测试
├── docker/mysql/init/         # MySQL 初始化脚本
├── scripts/                   # 启动和初始化脚本
├── docs/                      # 需求、接口、设计文档
├── compose.yaml               # MySQL 编排
├── .env.example               # 环境变量模板
└── README.md
```

## 本地前置环境

建议使用以下环境：

- `Docker Desktop`，并确保 `docker compose` 可用
- `Node.js` 20.19+ 或 22.12+；本项目演示环境推荐使用 Node.js 24.x
- `npm` 10+
- `Java` / `JDK` 21
- `Python` 3.10+

说明：

- 当前已验证环境：Docker 29.5.3、Docker Compose 5.1.4、Node.js 24.16.0、npm 11.13.0、JDK 21。
- 前端依赖中的 Vite 8 要求 Node.js `^20.19.0 || >=22.12.0`。
- 后端使用 Maven Wrapper，不要求手动安装 Maven；wrapper 会下载并使用 Maven 3.9.15。
- AI 服务依赖要求 Python 3.10+，当前演示环境安装 FastAPI 0.138.0。
- 默认本地端口：前端 `5173`，后端 `8080`，AI 服务 `8000`，MySQL `3306`。

建议先检查：

```bash
docker --version
docker compose version
node -v
npm -v
java -version
python3 --version
```

## 环境变量

首次启动前先复制模板：

Mac / Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

常用配置项：

- `VITE_HOST` / `VITE_PORT`：前端开发服务器地址和端口
- `VITE_API_PROXY_TARGET`：Vite 代理目标，默认指向 `http://127.0.0.1:8080`
- `SERVER_PORT`：Spring Boot 端口
- `MYSQL_PORT`：映射到宿主机的 MySQL 端口
- `MYSQL_DATABASE` / `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD`
- `AI_SERVICE_HOST` / `AI_SERVICE_PORT`：FastAPI 服务监听地址
- `AI_API_KEY`：真实 AI 能力所需，留空时 FastAPI 接口会返回 503
- `AI_API_BASE_URL` / `AI_MODEL`：OpenAI Compatible API 配置
- `JWT_SECRET` / `JWT_EXPIRE_MINUTES`
- `VITE_MOCK_API=true`：前端 API 走本地 mock
- `VITE_MOCK_AUTH=true`：登录注册走本地 mock

说明：

- Spring Boot 会直接读取仓库根目录的 `.env`
- 前端的 `vite.config.ts` 也会从仓库根目录加载 `.env`
- 如只做前后端基础联调，不必配置 `AI_API_KEY`

## 快速启动

推荐启动顺序：数据库 -> Spring Boot -> 前端。

如果你需要真实 AI 输出，再额外启动 FastAPI AI 服务并填写 `AI_API_KEY`。

### 1. 初始化

Mac / Linux:

```bash
./scripts/bootstrap.sh
```

Windows PowerShell:

```powershell
.\scripts\bootstrap.ps1
```

这个步骤会：

- 不存在 `.env` 时自动从 `.env.example` 复制
- 安装前端依赖

### 2. 启动 MySQL

Mac / Linux:

```bash
./scripts/start-db.sh
```

Windows PowerShell:

```powershell
.\scripts\start-db.ps1
```

或直接执行：

```bash
docker compose up -d
docker compose ps
```

### 3. 启动 Spring Boot 后端

Mac / Linux:

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

健康检查：

- `http://127.0.0.1:8080/api/health`
- `http://127.0.0.1:8080/actuator/health`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认访问地址：

- `http://127.0.0.1:5173`

### 5. 可选：启动 FastAPI AI 服务

Mac / Linux:

```bash
./scripts/start-ai.sh
```

Windows PowerShell:

```powershell
.\scripts\start-ai.ps1
```

这个脚本会：

- 自动创建 `backend/.venv`
- 安装 `backend/src/main/python/requirements.txt`
- 按 `.env` 中的 `AI_SERVICE_HOST` / `AI_SERVICE_PORT` 启动 FastAPI

默认地址：

- `http://127.0.0.1:8000/health`
- `http://127.0.0.1:8000/docs`

## 运行模式说明

### 模式 1：纯前端开发

设置：

```env
VITE_MOCK_API=true
VITE_MOCK_AUTH=true
```

此时只需要启动前端。

### 模式 2：前后端联调，不接真实 AI

设置：

- 不开启 `VITE_MOCK_API`
- 启动 MySQL、Spring Boot、前端
- 可以不启动 FastAPI
- 可以不填写 `AI_API_KEY`

此时 AI 相关接口由 Spring Boot 调用失败后回退到内置 mock 结果，适合功能联调和页面演示。

### 模式 3：完整联调，接真实 AI

设置：

- 启动 MySQL、Spring Boot、前端、FastAPI
- 在 `.env` 中填写有效的 `AI_API_KEY`
- 如需切换模型或供应商，调整 `AI_API_BASE_URL` 与 `AI_MODEL`

## 已实现的主要页面

前端当前已包含这些主路由：

- `/login`：登录
- `/register`：注册
- `/home`：首页总览
- `/health-data`：健康数据管理
- `/health-trends`：健康趋势
- `/ai-profile`：AI 健康画像
- `/ai-plan`：AI 健康计划
- `/knowledge`：健康知识与推送
- `/reminders`：提醒管理
- `/profile`：个人中心
- `/admin/users`：管理员用户管理
- `/admin/statistics`：管理员统计

## 主要接口入口

认证与用户：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me`
- `PUT /api/users/me/password`

健康数据：

- `POST /api/health-data`
- `GET /api/health-data`
- `POST /api/health-data/import`
- `DELETE /api/health-data/{dataId}`
- `GET /api/health-data/trends`

AI 能力：

- `POST /api/ai/profile`
- `POST /api/ai/plan`
- `POST /api/ai/knowledge-push`

知识与提醒：

- `GET /api/knowledge`
- `GET /api/knowledge/pushes`
- `PATCH /api/knowledge/pushes/{pushId}/read`
- `GET /api/reminders`
- `POST /api/reminders`
- `PUT /api/reminders/{reminderId}`
- `PATCH /api/reminders/{reminderId}/status`

管理员：

- `GET /api/admin/users`
- `PATCH /api/admin/users/{userId}/status`
- `GET /api/admin/login-logs`
- `GET /api/admin/statistics`

更详细的字段说明见 [docs/api/后端接口草案.md](docs/api/后端接口草案.md)。

### CSV 导入模板

前端静态目录提供模板：

- [frontend/public/health_data_template.csv](frontend/public/health_data_template.csv)

CSV 表头必须为：

```text
dataType,dataValue,unit,recordTime
```

`recordTime` 格式必须为：

```text
yyyy-MM-dd HH:mm:ss
```

支持的数据类型：

- `BLOOD_PRESSURE`
- `BLOOD_GLUCOSE`
- `WEIGHT`
- `EXERCISE_MINUTES`
- `SLEEP_HOURS`

## 测试

Java 单元测试：

```bash
cd backend
./mvnw test
```

Python 测试：

```bash
cd backend
python3 -m venv .venv
.venv/bin/pip install -r src/main/python/requirements.txt
.venv/bin/pip install pytest
.venv/bin/pytest src/test/python
```

说明：

- Java 侧已有健康数据服务测试
- Python 侧已有 FastAPI AI 接口测试
- 当前根目录没有统一的一键测试脚本

## 文档索引

需求与设计文档：

- [docs/requirements/需求规格说明书v2.md](docs/requirements/需求规格说明书v2.md)
- [docs/requirements/软件体系结构设计文档v2.md](docs/requirements/软件体系结构设计文档v2.md)
- [docs/database/数据库设计草案.md](docs/database/数据库设计草案.md)

后端与 AI 说明：

- [docs/api/后端接口草案.md](docs/api/后端接口草案.md)
- [docs/backend/登录注册后端实现说明.md](docs/backend/登录注册后端实现说明.md)
- [docs/backend/健康数据导入后端实现说明.md](docs/backend/健康数据导入后端实现说明.md)
- [docs/backend/人工智能模块.md](docs/backend/人工智能模块.md)

前端说明：

- [frontend/README.md](frontend/README.md)
- [docs/frontend/个人信息管理前端实现说明.md](docs/frontend/个人信息管理前端实现说明.md)
- [docs/frontend/健康数据管理前端实现说明.md](docs/frontend/健康数据管理前端实现说明.md)
