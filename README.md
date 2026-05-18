# AI个性化私人健康管理系统

当前仓库已经完成最小项目骨架初始化：

- `frontend/`: Vue 3.5 + Vite 8 + TypeScript
- `backend/`: Spring Boot 3.5.7 + Java 21 + Maven Wrapper
- `compose.yaml`: 本地 MySQL 8.4 Docker 编排
- `scripts/`: 初始化与本地启动脚本
- `docs/`: 项目文档占位

## 目录结构

```text
.
├── backend
├── docker
│   └── mysql
├── docs
│   └── requirements
├── frontend
├── scripts
├── compose.yaml
├── .env.example
└── README.md
```

需求说明书位置：

- `docs/requirements/需求规格说明书.md`

## 本机前置环境

克隆后要能按文档启动，机器上至少需要具备：

- `Docker Desktop` 29+，并确保 `docker compose` 可用
- `Node.js` 24+ 与 `npm` 11+
- `Java` 21
- 可访问 `npm`、`Maven Central`、`Docker Hub` 的网络环境

建议先自行确认：

```bash
docker --version
docker compose version
node -v
npm -v
java -version
```

如果网络环境受限，需要提前配置代理，否则可能在以下阶段失败：

- `npm install`
- `./mvnw spring-boot:run`
- `docker compose up -d`

## 克隆后先做什么

### 1. 可选：先跑初始化脚本

Mac / Linux:

```bash
./scripts/bootstrap.sh
```

Windows PowerShell:

```powershell
.\scripts\bootstrap.ps1
```

这个脚本会：

- 自动复制 `.env.example` 为 `.env`（如果 `.env` 还不存在）
- 安装前端依赖

### 2. 复制环境变量模板

Mac / Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

### 3. 修改 `.env` 中你本机需要调整的值

最常改的配置：

- `MYSQL_PORT`: 如果你本机已经装了 MySQL 或 3306 被占用，改成别的端口，例如 `3307`
- `MYSQL_ROOT_PASSWORD`: 建议改掉默认值
- `SERVER_PORT`: 如果后端 8080 被占用，改成别的端口
- `VITE_PORT`: 如果前端 5173 被占用，改成别的端口
- `VITE_API_PROXY_TARGET`: 如果你改了后端端口，这里也要一起改，例如 `http://127.0.0.1:8081`
- `DB_HOST`: 如果后端不是连本机 Docker MySQL，而是连别的数据库，在这里改
- `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD`: 默认先不要启用；只有后端要连一套单独的数据库时，才取消 `.env` 里的注释并填写
- `CORS_ALLOWED_ORIGINS`: 如果有人把前端跑在别的端口，把对应地址追加到这里

### 4. 可选：复制后端本地覆盖配置

后端默认会从仓库根目录的 `.env` 读取数据库和端口配置。  
如果你需要给 Spring Boot 单独覆盖配置，再复制下面这个模板：

Mac / Linux:

```bash
cp backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```

Windows PowerShell:

```powershell
Copy-Item backend/src/main/resources/application-local.yml.example backend/src/main/resources/application-local.yml
```

重点说明：

- 如果只是数据库端口冲突，优先改根目录 `.env` 里的 `MYSQL_PORT`
- 在默认本地开发模式下，后端会自动跟随 `MYSQL_PORT` 连接 Docker MySQL，不需要额外改 `DB_PORT`
- `application-local.yml` 主要用于后端单独覆盖，例如临时切换远程数据库、调整日志级别、单独改后端端口

## 启动数据库

也可以直接用脚本：

Mac / Linux:

```bash
./scripts/start-db.sh
```

Windows PowerShell:

```powershell
.\scripts\start-db.ps1
```

等价手动命令：

```bash
docker compose up -d
```

查看状态：

```bash
docker compose ps
```

停止数据库：

```bash
docker compose down
```

如果你需要连容器内 MySQL：

```bash
docker compose exec mysql mysql -u root -p
```

## 启动前端

Mac / Linux:

```bash
cd frontend
npm install
npm run dev
```

Windows PowerShell:

```powershell
Set-Location frontend
npm install
npm run dev
```

默认访问地址：

- `http://127.0.0.1:5173`

## 启动后端

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

默认访问地址：

- `http://127.0.0.1:8080/api/health`

## 一键入口脚本

仓库提供了跨平台脚本入口：

- `scripts/bootstrap.sh`
- `scripts/bootstrap.ps1`
- `scripts/start-db.sh`
- `scripts/start-db.ps1`
- `scripts/start-dev.sh`
- `scripts/start-dev.ps1`

说明：

- `bootstrap.*` 负责准备 `.env` 和安装前端依赖
- `start-db.*` 负责启动数据库
- `start-dev.*` 负责提示完整启动顺序，并先启动数据库
- 为了避免脚本在不同终端环境下行为不一致，`start-dev.*` 不自动创建多个终端窗口，而是明确打印后端和前端启动命令

## 当前脚手架说明

- 前端已经切换为项目首页占位，不再保留 Vite 演示页
- 后端已经提供最小健康检查接口和开放式开发环境安全配置
- 数据库目前只初始化容器和库连接约定，业务表结构后续再按模块推进
