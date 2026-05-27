# AI健康分析模块 — FastAPI 服务设计

**日期**: 2026-05-27
**分支**: feature/ai_advice
**关联需求**: 需求规格说明书 3.3 节 — AI健康分析模块

## 1. 架构概述

FastAPI 作为纯 AI 分析服务，不直接访问 MySQL。Spring Boot 查询数据库后将健康数据传入 FastAPI，FastAPI 调用 OpenAI API 生成分析结果返回，Spring Boot 负责持久化。

```
Spring Boot (8080)                    FastAPI (30000)                  OpenAI API
    │                                     │                               │
    ├─ POST /api/ai/profile ──────────────→ ProfileAgent ──────────────→ │
    │   {userId, healthData, timeRange}    │                               │
    │←──────────────────────────────────── healthTags, riskLevel, analysis │
    │                                     │                               │
    ├─ POST /api/ai/plan ─────────────────→ PlanAgent ──────────────────→ │
    │   {userId, healthData, healthNeed}   │                               │
    │←──────────────────────────────────── planName, diet/sport/sleep      │
    │                                     │                               │
    ├─ POST /api/ai/knowledge-push ───────→ PushAgent ──────────────────→ │
    │   {profileId, healthTags,           │                               │
    │    knowledgeItems}                  │                               │
    │←──────────────────────────────────── selectedKnowledgeIds           │
```

## 2. 目录结构

```
backend/src/main/python/
├── main.py                     # FastAPI 入口，启动时校验配置
├── requirements.txt            # 已存在
├── config.py                   # 从 .env 读取 AI 配置
├── prompts/                    # Prompt 模板（.md 文件，开发时直接编辑）
│   ├── profile.md
│   ├── plan.md
│   └── push.md
├── agents/
│   ├── __init__.py
│   ├── base.py                 # Agent 工厂：OpenAIChatCompletionClient + AssistantAgent
│   ├── profile_agent.py
│   ├── plan_agent.py
│   └── push_agent.py
├── routers/
│   ├── __init__.py
│   └── ai.py                   # 三个 AI 端点
├── models/
│   ├── __init__.py
│   └── schemas.py              # Pydantic 请求/响应模型
└── services/
    ├── __init__.py
    └── ai_service.py           # 业务编排层

backend/src/test/python/
├── __init__.py
├── conftest.py                 # pytest fixtures + Agent mock
├── test_config.py
├── test_profile.py
├── test_plan.py
└── test_push.py
```

## 3. 模块职责

| 模块 | 职责 |
|---|---|
| `config.py` | 读取 `.env` 中的 AI_API_KEY, AI_API_BASE_URL, AI_MODEL，启动时校验必填项 |
| `agents/base.py` | 工厂函数，创建 OpenAIChatCompletionClient 和 AssistantAgent，三个 Agent 复用 |
| `agents/profile_agent.py` | 加载 `prompts/profile.md`，分析健康数据，返回画像 JSON |
| `agents/plan_agent.py` | 加载 `prompts/plan.md`，根据健康需求生成计划 JSON |
| `agents/push_agent.py` | 加载 `prompts/push.md`，匹配标签与知识，返回推荐ID列表 |
| `models/schemas.py` | Pydantic 模型：请求/响应/内部数据传递 |
| `services/ai_service.py` | 组装健康数据→自然语言→调用Agent→解析JSON→返回结构化结果 |
| `routers/ai.py` | HTTP 层：参数校验、API_KEY 检查、委托 service |

## 4. Prompt 设计

三个 Agent 的 system prompt 均从 `prompts/*.md` 加载。

- **profile.md**: 输入健康数据列表 → 输出 `{healthTags, riskLevel, analysis}`
- **plan.md**: 输入健康需求+数据 → 输出 `{planName, dietSuggest, sportSuggest, sleepSuggest}`
- **push.md**: 输入健康标签+候选知识 → 输出 `{selectedKnowledgeIds, reason}`

每个 prompt 末尾强调：**仅返回JSON，不要额外文字**。

## 5. 数据流（以 profile 为例）

1. Sping Boot → FastAPI: `{userId, healthData: [{type, value, recordTime}], timeRange}`
2. `routers/ai.py` 校验 `AI_API_KEY` 存在，解析请求体为 `ProfileRequest`
3. `ai_service.generate_profile()` 将 healthData 转为自然语言描述
4. `profile_agent.run(context)` 调用 AssistantAgent → OpenAI API
5. 解析 JSON 响应为 `ProfileResult`
6. FastAPI → Spring Boot: `{healthTags, riskLevel, analysis}`
7. Spring Boot 写入 `health_profile` 表，返回前端

## 6. 错误处理

| 场景 | HTTP 状态码 | 行为 |
|---|---|---|
| AI_API_KEY 为空 | 503 | 返回 `{code: 1, message: "AI service not configured"}` |
| 请求参数校验失败 | 422 | FastAPI 自动校验，Pydantic 返回详情 |
| Agent 返回非法 JSON | 502 | 捕获解析异常，返回 `{code: 1, message: "AI response parse error"}` |
| OpenAI API 调用失败 | 502 | 捕获网络/API异常，记录日志，返回友好错误 |

## 7. 测试策略（12个用例）

### 测试文件与用例

**conftest.py** — 共享 fixtures：
- `mock_openai_client`: Mock `OpenAIChatCompletionClient`
- `mock_agent_factory`: Mock Agent 工厂，返回预设响应的 AsyncMock Agent
- `test_client`: FastAPI TestClient，注入 mock 依赖

**test_config.py（1个）**：
- 配置正确加载 `.env` 值

**test_profile.py（5个）**：
- 正常请求返回完整画像 JSON
- Agent 返回非法 JSON → 502
- API_KEY 为空 → 503
- healthData 为空数组 → 422
- timeRange 非法值 → 422

**test_plan.py（3个）**：
- 正常请求返回计划 JSON
- healthNeed 为空 → 422
- Agent JSON 解析失败 → 502

**test_push.py（3个）**：
- 正常请求返回推荐知识ID列表
- 候选知识列表为空 → 返回空列表
- profileId 缺失 → 422

### 运行方式

```bash
cd backend
.venv/bin/pip install pytest pytest-asyncio httpx
.venv/bin/python -m pytest src/test/python/ -v
```

## 8. 可测试性设计

- `routers/ai.py` 通过 FastAPI `Depends()` 注入 service，测试时替换 mock
- `ai_service.py` 通过构造函数接收 Agent 实例，不硬编码
- Agent 的 `run()` 是标准异步接口，`AsyncMock` 直接覆盖
