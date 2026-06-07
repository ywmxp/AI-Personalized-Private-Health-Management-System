# AI健康分析模块 FastAPI 服务 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于FastAPI + Microsoft Agent Framework的AI健康分析服务，提供健康画像、健康计划、知识推送三个端点。

**Architecture:** FastAPI作为纯AI分析服务，健康数据由Spring Boot传入，Agent调用OpenAI API生成结构化JSON结果后返回。不直接访问MySQL。

**Tech Stack:** Python 3.12, FastAPI, AutoGen 0.4 (AssistantAgent + OpenAIChatCompletionClient), Pydantic, pytest + pytest-asyncio

---

### Task 1: 创建目录结构 + prompts/*.md

**Files:**
- Create: `backend/src/main/python/prompts/profile.md`
- Create: `backend/src/main/python/prompts/plan.md`
- Create: `backend/src/main/python/prompts/push.md`

- [ ] **Step 1: 创建 prompts/profile.md**

```bash
mkdir -p backend/src/main/python/prompts
```

File `backend/src/main/python/prompts/profile.md`:

```markdown
你是一个专业的健康管理分析助手。根据用户提供的健康数据，生成个性化健康画像报告。

## 输入
用户会提供一段时间内的健康数据记录，包含数据类型、数值、记录时间。

## 输出要求
分析数据后，严格按以下JSON格式输出，不要包含任何其他文字：

```json
{
  "healthTags": ["标签1", "标签2"],
  "riskLevel": "LOW",
  "analysis": "简要的健康分析描述"
}
```

## 规则
- healthTags: 根据数据提炼2-5个健康标签，使用中文，如"睡眠不足""运动达标""体重偏高""血压偏高""血糖正常"
- riskLevel: LOW（各项指标正常）、MEDIUM（部分指标偏离正常范围）、HIGH（多项指标严重偏离）
- analysis: 100-200字的综合健康分析，指出关键问题和改进方向
- riskLevel为LOW时，healthTags至少包含一个正向标签
- 不做医疗诊断，仅提供生活方式参考
- 仅返回JSON，不要额外文字
```

- [ ] **Step 2: 创建 prompts/plan.md**

File `backend/src/main/python/prompts/plan.md`:

```markdown
你是一个专业的健康管理顾问。根据用户的健康需求和当前健康数据，生成个性化健康计划。

## 输入
用户会提供：1）健康需求描述 2）当前健康数据概况

## 输出要求
严格按以下JSON格式输出，不要包含任何其他文字：

```json
{
  "planName": "计划名称",
  "dietSuggest": "饮食建议内容",
  "sportSuggest": "运动建议内容",
  "sleepSuggest": "睡眠建议内容"
}
```

## 规则
- planName: 10字以内的计划名称，简洁有针对性
- dietSuggest: 100-200字的饮食建议，具体可执行
- sportSuggest: 100-200字的运动建议，考虑数据中的运动情况
- sleepSuggest: 100-200字的睡眠建议，针对睡眠数据给出改善方法
- 建议要具体、可执行，避免空泛的"多吃蔬菜""多运动"
- 不做医疗诊断，仅提供生活方式参考
- 仅返回JSON，不要额外文字
```

- [ ] **Step 3: 创建 prompts/push.md**

File `backend/src/main/python/prompts/push.md`:

```markdown
你是一个健康知识推荐助手。根据用户的健康画像标签，从候选知识库中匹配最相关的健康知识。

## 输入
用户会提供：1）健康标签列表 2）候选知识列表（含ID、标题、标签）

## 输出要求
严格按以下JSON格式输出，不要包含任何其他文字：

```json
{
  "selectedKnowledgeIds": [1, 3],
  "reason": "选择理由简述"
}
```

## 规则
- selectedKnowledgeIds: 推荐的知识ID数组，按相关度降序排列，最多5条
- reason: 30字以内简述推荐理由
- 优先匹配标签完全一致的知识，其次部分匹配
- 如果没有匹配的知识，返回空数组
- 仅返回JSON，不要额外文字
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/python/prompts/
git commit -m "feat: add AI agent prompt templates for profile, plan, and push"
```

---

### Task 2: config.py

**Files:**
- Create: `backend/src/main/python/config.py`
- Create: `backend/src/test/python/__init__.py`
- Create: `backend/src/test/python/test_config.py`
- Create: `backend/src/test/python/conftest.py` (部分)

- [ ] **Step 1: 创建测试目录和空 __init__.py**

```bash
mkdir -p backend/src/test/python
touch backend/src/test/python/__init__.py
```

- [ ] **Step 2: 写 test_config.py（测试先行）**

File `backend/src/test/python/test_config.py`:

```python
import os
import sys
from pathlib import Path

# Add source to path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))


def test_settings_from_env(monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "sk-test-key")
    monkeypatch.setenv("AI_API_BASE_URL", "https://test.api.com/v1")
    monkeypatch.setenv("AI_MODEL", "gpt-4o")

    # Re-import to pick up env vars
    import config
    import importlib
    importlib.reload(config)

    assert config.settings.AI_API_KEY == "sk-test-key"
    assert config.settings.AI_API_BASE_URL == "https://test.api.com/v1"
    assert config.settings.AI_MODEL == "gpt-4o"


def test_settings_defaults(monkeypatch):
    monkeypatch.delenv("AI_API_KEY", raising=False)
    monkeypatch.delenv("AI_API_BASE_URL", raising=False)
    monkeypatch.delenv("AI_MODEL", raising=False)

    import config
    import importlib
    importlib.reload(config)

    assert config.settings.AI_API_KEY == ""
    assert config.settings.AI_API_BASE_URL == "https://api.openai.com/v1"
    assert config.settings.AI_MODEL == "gpt-4o-mini"
```

- [ ] **Step 3: 运行测试确认失败**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_config.py -v
```

Expected: FAIL — `ModuleNotFoundError: No module named 'config'`

- [ ] **Step 4: 实现 config.py**

File `backend/src/main/python/config.py`:

```python
import os
from pathlib import Path
from dotenv import load_dotenv


def _find_and_load_env() -> None:
    current = Path(__file__).resolve().parent
    for parent in [current] + list(current.parents):
        env_file = parent / ".env"
        if env_file.is_file():
            load_dotenv(env_file)
            return


_find_and_load_env()


class Settings:
    AI_API_KEY: str = os.getenv("AI_API_KEY", "")
    AI_API_BASE_URL: str = os.getenv("AI_API_BASE_URL", "https://api.openai.com/v1")
    AI_MODEL: str = os.getenv("AI_MODEL", "gpt-4o-mini")


settings = Settings()
```

- [ ] **Step 5: 运行测试确认通过**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_config.py -v
```

Expected: 2 PASS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/python/config.py backend/src/test/python/__init__.py backend/src/test/python/test_config.py
git commit -m "feat: add config.py with .env loading and tests"
```

---

### Task 3: models/schemas.py

**Files:**
- Create: `backend/src/main/python/models/__init__.py`
- Create: `backend/src/main/python/models/schemas.py`

- [ ] **Step 1: 创建 models 目录**

```bash
mkdir -p backend/src/main/python/models
touch backend/src/main/python/models/__init__.py
```

- [ ] **Step 2: 实现 schemas.py**

File `backend/src/main/python/models/schemas.py`:

```python
from pydantic import BaseModel, Field


class HealthDataItem(BaseModel):
    data_type: str = Field(..., alias="dataType")
    data_value: str = Field(..., alias="dataValue")
    record_time: str = Field(..., alias="recordTime")


class ProfileRequest(BaseModel):
    user_id: int = Field(..., alias="userId")
    health_data: list[HealthDataItem] = Field(..., alias="healthData", min_length=1)
    time_range: str = Field(..., alias="timeRange")


class ProfileResponse(BaseModel):
    health_tags: list[str] = Field(..., alias="healthTags")
    risk_level: str = Field(..., alias="riskLevel")
    analysis: str


class PlanRequest(BaseModel):
    user_id: int = Field(..., alias="userId")
    health_data: list[HealthDataItem] = Field(..., alias="healthData", min_length=1)
    health_need: str = Field(..., alias="healthNeed", min_length=1)


class PlanResponse(BaseModel):
    plan_name: str = Field(..., alias="planName")
    diet_suggest: str = Field(..., alias="dietSuggest")
    sport_suggest: str = Field(..., alias="sportSuggest")
    sleep_suggest: str = Field(..., alias="sleepSuggest")


class KnowledgeItem(BaseModel):
    knowledge_id: int = Field(..., alias="knowledgeId")
    title: str
    relate_tag: str = Field(..., alias="relateTag")


class PushRequest(BaseModel):
    profile_id: int = Field(..., alias="profileId")
    health_tags: list[str] = Field(..., alias="healthTags", min_length=1)
    knowledge_items: list[KnowledgeItem] = Field(..., alias="knowledgeItems")


class PushResponse(BaseModel):
    selected_knowledge_ids: list[int] = Field(..., alias="selectedKnowledgeIds")
    reason: str
```

- [ ] **Step 3: 验证模型可导入**

```bash
cd backend && .venv/Scripts/python.exe -c "from models.schemas import ProfileRequest, PlanRequest, PushRequest; print('OK')"
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/python/models/
git commit -m "feat: add Pydantic request/response schemas for AI endpoints"
```

---

### Task 4: agents/base.py

**Files:**
- Create: `backend/src/main/python/agents/__init__.py`
- Create: `backend/src/main/python/agents/base.py`

- [ ] **Step 1: 创建 agents 目录**

```bash
mkdir -p backend/src/main/python/agents
touch backend/src/main/python/agents/__init__.py
```

- [ ] **Step 2: 实现 base.py**

File `backend/src/main/python/agents/base.py`:

```python
from autogen_ext.models.openai import OpenAIChatCompletionClient
from autogen_agentchat.agents import AssistantAgent
from config import settings


def create_model_client() -> OpenAIChatCompletionClient:
    return OpenAIChatCompletionClient(
        model=settings.AI_MODEL,
        api_key=settings.AI_API_KEY,
        base_url=settings.AI_API_BASE_URL,
    )


def create_agent(name: str, system_message: str) -> AssistantAgent:
    client = create_model_client()
    return AssistantAgent(
        name=name,
        model_client=client,
        system_message=system_message,
    )


def load_prompt(filename: str) -> str:
    import os
    prompt_dir = os.path.join(os.path.dirname(__file__), "..", "prompts")
    with open(os.path.join(prompt_dir, filename), "r", encoding="utf-8") as f:
        return f.read()
```

- [ ] **Step 3: 验证可导入**

```bash
cd backend/src/main/python && ../../.venv/Scripts/python.exe -c "from agents.base import create_agent, load_prompt; print('OK')"
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/python/agents/
git commit -m "feat: add agent factory and prompt loader"
```

---

### Task 5: agents/profile_agent.py

**Files:**
- Create: `backend/src/main/python/agents/profile_agent.py`
- Create: `backend/src/test/python/test_profile.py`

- [ ] **Step 1: 写 test_profile.py（测试先行）**

File `backend/src/test/python/test_profile.py`:

```python
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))

import pytest
from agents.profile_agent import ProfileAgent


class TestProfileAgent:
    def test_loads_prompt(self):
        agent = ProfileAgent()
        assert len(agent.system_message) > 0
        assert "健康标签" in agent.system_message

    @pytest.mark.asyncio
    async def test_parse_valid_json_response(self):
        agent = ProfileAgent()
        result = agent.parse_response(
            '{"healthTags": ["睡眠不足"], "riskLevel": "LOW", "analysis": "注意休息"}'
        )
        assert result["healthTags"] == ["睡眠不足"]
        assert result["riskLevel"] == "LOW"
        assert result["analysis"] == "注意休息"

    @pytest.mark.asyncio
    async def test_parse_invalid_json_raises(self):
        agent = ProfileAgent()
        with pytest.raises(ValueError):
            agent.parse_response("not json")
```

- [ ] **Step 2: 运行测试确认失败**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_profile.py -v
```

Expected: FAIL — `ModuleNotFoundError: No module named 'agents.profile_agent'`

- [ ] **Step 3: 实现 profile_agent.py**

File `backend/src/main/python/agents/profile_agent.py`:

```python
import json
from agents.base import create_agent, load_prompt


class ProfileAgent:
    def __init__(self):
        self.system_message = load_prompt("profile.md")
        self._agent = create_agent("profile_agent", self.system_message)

    async def run(self, health_context: str) -> str:
        result = await self._agent.run(task=health_context)
        content = result.messages[-1].content
        return content

    @staticmethod
    def parse_response(raw: str) -> dict:
        text = raw.strip()
        if text.startswith("```"):
            lines = text.split("\n")
            lines = [l for l in lines if not l.startswith("```")]
            text = "\n".join(lines)
        return json.loads(text)
```

- [ ] **Step 4: 运行测试确认通过**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_profile.py -v
```

Expected: 3 PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/python/agents/profile_agent.py backend/src/test/python/test_profile.py
git commit -m "feat: add ProfileAgent with prompt loading and JSON parsing"
```

---

### Task 6: agents/plan_agent.py + agents/push_agent.py

**Files:**
- Create: `backend/src/main/python/agents/plan_agent.py`
- Create: `backend/src/main/python/agents/push_agent.py`
- Create: `backend/src/test/python/test_plan.py`
- Create: `backend/src/test/python/test_push.py`

- [ ] **Step 1: 写 test_plan.py（测试先行）**

File `backend/src/test/python/test_plan.py`:

```python
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))

import pytest
from agents.plan_agent import PlanAgent


class TestPlanAgent:
    def test_loads_prompt(self):
        agent = PlanAgent()
        assert len(agent.system_message) > 0
        assert "健康计划" in agent.system_message

    @pytest.mark.asyncio
    async def test_parse_valid_json_response(self):
        agent = PlanAgent()
        result = agent.parse_response(
            '{"planName": "改善睡眠计划", "dietSuggest": "晚餐清淡", '
            '"sportSuggest": "每天散步30分钟", "sleepSuggest": "固定就寝时间"}'
        )
        assert result["planName"] == "改善睡眠计划"
        assert result["dietSuggest"] == "晚餐清淡"
        assert result["sportSuggest"] == "每天散步30分钟"
        assert result["sleepSuggest"] == "固定就寝时间"

    @pytest.mark.asyncio
    async def test_parse_invalid_json_raises(self):
        agent = PlanAgent()
        with pytest.raises(ValueError):
            agent.parse_response("not json")
```

- [ ] **Step 2: 写 test_push.py（测试先行）**

File `backend/src/test/python/test_push.py`:

```python
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))

import pytest
from agents.push_agent import PushAgent


class TestPushAgent:
    def test_loads_prompt(self):
        agent = PushAgent()
        assert len(agent.system_message) > 0
        assert "健康知识" in agent.system_message

    @pytest.mark.asyncio
    async def test_parse_valid_json_response(self):
        agent = PushAgent()
        result = agent.parse_response(
            '{"selectedKnowledgeIds": [1, 3], "reason": "标签匹配"}'
        )
        assert result["selectedKnowledgeIds"] == [1, 3]
        assert result["reason"] == "标签匹配"

    @pytest.mark.asyncio
    async def test_parse_empty_ids_returns_empty_list(self):
        agent = PushAgent()
        result = agent.parse_response(
            '{"selectedKnowledgeIds": [], "reason": "无匹配"}'
        )
        assert result["selectedKnowledgeIds"] == []

    @pytest.mark.asyncio
    async def test_parse_invalid_json_raises(self):
        agent = PushAgent()
        with pytest.raises(ValueError):
            agent.parse_response("not json")
```

- [ ] **Step 3: 运行测试确认失败**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_plan.py src/test/python/test_push.py -v
```

Expected: FAIL — module not found

- [ ] **Step 4: 实现 plan_agent.py**

File `backend/src/main/python/agents/plan_agent.py`:

```python
import json
from agents.base import create_agent, load_prompt


class PlanAgent:
    def __init__(self):
        self.system_message = load_prompt("plan.md")
        self._agent = create_agent("plan_agent", self.system_message)

    async def run(self, health_context: str) -> str:
        result = await self._agent.run(task=health_context)
        content = result.messages[-1].content
        return content

    @staticmethod
    def parse_response(raw: str) -> dict:
        text = raw.strip()
        if text.startswith("```"):
            lines = text.split("\n")
            lines = [l for l in lines if not l.startswith("```")]
            text = "\n".join(lines)
        return json.loads(text)
```

- [ ] **Step 5: 实现 push_agent.py**

File `backend/src/main/python/agents/push_agent.py`:

```python
import json
from agents.base import create_agent, load_prompt


class PushAgent:
    def __init__(self):
        self.system_message = load_prompt("push.md")
        self._agent = create_agent("push_agent", self.system_message)

    async def run(self, health_context: str) -> str:
        result = await self._agent.run(task=health_context)
        content = result.messages[-1].content
        return content

    @staticmethod
    def parse_response(raw: str) -> dict:
        text = raw.strip()
        if text.startswith("```"):
            lines = text.split("\n")
            lines = [l for l in lines if not l.startswith("```")]
            text = "\n".join(lines)
        return json.loads(text)
```

- [ ] **Step 6: 运行测试确认通过**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_plan.py src/test/python/test_push.py -v
```

Expected: 7 PASS

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/python/agents/plan_agent.py backend/src/main/python/agents/push_agent.py backend/src/test/python/test_plan.py backend/src/test/python/test_push.py
git commit -m "feat: add PlanAgent and PushAgent with tests"
```

---

### Task 7: services/ai_service.py

**Files:**
- Create: `backend/src/main/python/services/__init__.py`
- Create: `backend/src/main/python/services/ai_service.py`

- [ ] **Step 1: 创建 services 目录**

```bash
mkdir -p backend/src/main/python/services
touch backend/src/main/python/services/__init__.py
```

- [ ] **Step 2: 实现 ai_service.py**

File `backend/src/main/python/services/ai_service.py`:

```python
import json
from models.schemas import (
    ProfileRequest,
    ProfileResponse,
    PlanRequest,
    PlanResponse,
    PushRequest,
    PushResponse,
)
from agents.profile_agent import ProfileAgent
from agents.plan_agent import PlanAgent
from agents.push_agent import PushAgent


def _format_health_data(health_data) -> str:
    lines = []
    for item in health_data:
        lines.append(f"- {item.record_time}: {item.data_type} = {item.data_value}")
    return "\n".join(lines)


class AIService:
    def __init__(self):
        self._profile_agent = ProfileAgent()
        self._plan_agent = PlanAgent()
        self._push_agent = PushAgent()

    async def generate_profile(self, request: ProfileRequest) -> ProfileResponse:
        context = f"时间范围: {request.time_range}\n\n健康数据:\n{_format_health_data(request.health_data)}"
        raw = await self._profile_agent.run(context)
        data = self._profile_agent.parse_response(raw)
        return ProfileResponse(
            healthTags=data["healthTags"],
            riskLevel=data["riskLevel"],
            analysis=data["analysis"],
        )

    async def generate_plan(self, request: PlanRequest) -> PlanResponse:
        data_lines = _format_health_data(request.health_data)
        context = f"健康需求: {request.health_need}\n\n当前健康数据:\n{data_lines}"
        raw = await self._plan_agent.run(context)
        data = self._plan_agent.parse_response(raw)
        return PlanResponse(
            planName=data["planName"],
            dietSuggest=data["dietSuggest"],
            sportSuggest=data["sportSuggest"],
            sleepSuggest=data["sleepSuggest"],
        )

    async def push_knowledge(self, request: PushRequest) -> PushResponse:
        tags = ", ".join(request.health_tags)
        knowledge_lines = []
        for item in request.knowledge_items:
            knowledge_lines.append(f"ID={item.knowledge_id}: {item.title} (标签: {item.relate_tag})")
        context = f"健康标签: {tags}\n\n候选知识:\n" + "\n".join(knowledge_lines)
        raw = await self._push_agent.run(context)
        data = self._push_agent.parse_response(raw)
        return PushResponse(
            selectedKnowledgeIds=data["selectedKnowledgeIds"],
            reason=data["reason"],
        )

    @staticmethod
    def check_api_key() -> bool:
        from config import settings
        return bool(settings.AI_API_KEY)
```

- [ ] **Step 3: 验证可导入**

```bash
cd backend/src/main/python && ../../.venv/Scripts/python.exe -c "from services.ai_service import AIService; print('OK')"
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/python/services/
git commit -m "feat: add AIService orchestration layer"
```

---

### Task 8: routers/ai.py

**Files:**
- Create: `backend/src/main/python/routers/ai.py`

- [ ] **Step 1: 写 conftest.py 的 mock fixtures**

File `backend/src/test/python/conftest.py`:

```python
import sys
from pathlib import Path
from unittest.mock import AsyncMock

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))

import pytest


class MockAgentRun:
    """Mixin providing async run() that returns a mock TaskResult."""
    async def run(self, task):
        result = AsyncMock()
        msg = AsyncMock()
        msg.content = self._mock_content
        result.messages = [msg]
        return result


class MockProfileAgent(MockAgentRun):
    _mock_content = '{"healthTags": ["睡眠不足"], "riskLevel": "LOW", "analysis": "注意休息"}'


class MockPlanAgent(MockAgentRun):
    _mock_content = '{"planName": "改善睡眠", "dietSuggest": "清淡饮食", "sportSuggest": "散步30分钟", "sleepSuggest": "固定就寝"}'


class MockPushAgent(MockAgentRun):
    _mock_content = '{"selectedKnowledgeIds": [1, 2], "reason": "标签匹配"}'


@pytest.fixture
def mock_ai_service(monkeypatch):
    from services import ai_service

    original_profile = ai_service.AIService.__init__

    def mock_init(self):
        self._profile_agent = MockProfileAgent()
        self._plan_agent = MockPlanAgent()
        self._push_agent = MockPushAgent()

    monkeypatch.setattr(ai_service.AIService, "__init__", mock_init)

    svc = ai_service.AIService()
    return svc


@pytest.fixture
def api_key_present(monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "sk-test-key")
    import config
    import importlib
    importlib.reload(config)
    return config


@pytest.fixture
def api_key_missing(monkeypatch):
    monkeypatch.delenv("AI_API_KEY", raising=False)
    import config
    import importlib
    importlib.reload(config)
    return config
```

- [ ] **Step 2: 实现 routers/ai.py**

File `backend/src/main/python/routers/ai.py`:

```python
from fastapi import APIRouter, Depends, HTTPException
from models.schemas import (
    ProfileRequest,
    ProfileResponse,
    PlanRequest,
    PlanResponse,
    PushRequest,
    PushResponse,
)
from services.ai_service import AIService

router = APIRouter(prefix="/api/ai", tags=["ai"])


def get_ai_service() -> AIService:
    if not AIService.check_api_key():
        raise HTTPException(status_code=503, detail="AI service not configured")
    return AIService()


@router.post("/profile", response_model=ProfileResponse)
async def generate_profile(
    request: ProfileRequest,
    svc: AIService = Depends(get_ai_service),
) -> ProfileResponse:
    try:
        return await svc.generate_profile(request)
    except ValueError as e:
        raise HTTPException(status_code=502, detail=f"AI response parse error: {str(e)}")


@router.post("/plan", response_model=PlanResponse)
async def generate_plan(
    request: PlanRequest,
    svc: AIService = Depends(get_ai_service),
) -> PlanResponse:
    try:
        return await svc.generate_plan(request)
    except ValueError as e:
        raise HTTPException(status_code=502, detail=f"AI response parse error: {str(e)}")


@router.post("/knowledge-push", response_model=PushResponse)
async def push_knowledge(
    request: PushRequest,
    svc: AIService = Depends(get_ai_service),
) -> PushResponse:
    try:
        return await svc.push_knowledge(request)
    except ValueError as e:
        raise HTTPException(status_code=502, detail=f"AI response parse error: {str(e)}")
```

- [ ] **Step 3: 更新 routers/__init__.py**

File `backend/src/main/python/routers/__init__.py`:

```python
from routers.ai import router as ai_router

__all__ = ["ai_router"]
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/python/routers/ backend/src/test/python/conftest.py
git commit -m "feat: add AI router endpoints and test fixtures"
```

---

### Task 9: main.py + 集成测试

**Files:**
- Write: `backend/src/main/python/main.py`
- Create: `backend/src/test/python/test_integration.py`

- [ ] **Step 1: 写集成测试 test_integration.py（测试先行）**

File `backend/src/test/python/test_integration.py`:

```python
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "main" / "python"))

import pytest
from fastapi import HTTPException
from fastapi.testclient import TestClient


@pytest.fixture
def client(api_key_present, mock_ai_service, monkeypatch):
    # Override get_ai_service to use mock
    from routers import ai
    from services.ai_service import AIService

    async def mock_get(profile_request, svc):
        return await mock_ai_service.generate_profile(profile_request)

    async def mock_plan(plan_request, svc):
        return await mock_ai_service.generate_plan(plan_request)

    async def mock_push(push_request, svc):
        return await mock_ai_service.push_knowledge(push_request)

    monkeypatch.setattr(ai, "get_ai_service", lambda: mock_ai_service)

    from main import app
    return TestClient(app)


class TestProfileEndpoint:
    def test_profile_returns_200(self, client):
        response = client.post("/api/ai/profile", json={
            "userId": 1,
            "healthData": [
                {"dataType": "SLEEP_HOURS", "dataValue": "5", "recordTime": "2026-05-20 08:00:00"}
            ],
            "timeRange": "LAST_7_DAYS",
        })
        assert response.status_code == 200
        data = response.json()
        assert "healthTags" in data
        assert data["healthTags"] == ["睡眠不足"]

    def test_profile_empty_health_data_returns_422(self, client):
        response = client.post("/api/ai/profile", json={
            "userId": 1,
            "healthData": [],
            "timeRange": "LAST_7_DAYS",
        })
        assert response.status_code == 422

    def test_profile_missing_fields_returns_422(self, client):
        response = client.post("/api/ai/profile", json={})
        assert response.status_code == 422


class TestPlanEndpoint:
    def test_plan_returns_200(self, client):
        response = client.post("/api/ai/plan", json={
            "userId": 1,
            "healthData": [
                {"dataType": "SLEEP_HOURS", "dataValue": "5", "recordTime": "2026-05-20 08:00:00"}
            ],
            "healthNeed": "改善睡眠",
        })
        assert response.status_code == 200
        data = response.json()
        assert data["planName"] == "改善睡眠"

    def test_plan_empty_health_need_returns_422(self, client):
        response = client.post("/api/ai/plan", json={
            "userId": 1,
            "healthData": [
                {"dataType": "SLEEP_HOURS", "dataValue": "5", "recordTime": "2026-05-20 08:00:00"}
            ],
            "healthNeed": "",
        })
        assert response.status_code == 422


class TestPushEndpoint:
    def test_push_returns_200(self, client):
        response = client.post("/api/ai/knowledge-push", json={
            "profileId": 1,
            "healthTags": ["睡眠不足"],
            "knowledgeItems": [
                {"knowledgeId": 1, "title": "睡眠健康", "relateTag": "睡眠不足"}
            ],
        })
        assert response.status_code == 200
        data = response.json()
        assert data["selectedKnowledgeIds"] == [1, 2]

    def test_push_empty_tags_returns_422(self, client):
        response = client.post("/api/ai/knowledge-push", json={
            "profileId": 1,
            "healthTags": [],
            "knowledgeItems": [],
        })
        assert response.status_code == 422


class TestApiKeyCheck:
    @pytest.fixture
    def client_no_key(self, api_key_missing, mock_ai_service, monkeypatch):
        def raise_503():
            raise HTTPException(status_code=503, detail="AI service not configured")
        monkeypatch.setattr("routers.ai.get_ai_service", raise_503)
        from main import app
        return TestClient(app)

    def test_no_api_key_returns_503(self, client_no_key):
        response = client_no_key.post("/api/ai/profile", json={
            "userId": 1,
            "healthData": [
                {"dataType": "SLEEP_HOURS", "dataValue": "5", "recordTime": "2026-05-20 08:00:00"}
            ],
            "timeRange": "LAST_7_DAYS",
        })
        assert response.status_code == 503
```

- [ ] **Step 2: 运行测试确认失败**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_integration.py -v
```

Expected: FAIL — `ModuleNotFoundError` or no `app` in main

- [ ] **Step 3: 实现 main.py**

File `backend/src/main/python/main.py`:

```python
import sys
from fastapi import FastAPI
from routers.ai import router as ai_router

app = FastAPI(title="AI Health Analysis Service", version="0.1.0")
app.include_router(ai_router)


@app.get("/health")
async def health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn

    host = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8000
    uvicorn.run(app, host=host, port=port)
```

- [ ] **Step 4: 运行集成测试**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/test_integration.py -v
```

Expected: 8 PASS (需要修复 conftest 中的 HTTPException import)

检查 test_integration.py 顶部缺少 `HTTPException` 导入，更新文件添加：
```python
from fastapi import HTTPException
```

- [ ] **Step 5: 运行全部测试**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/ -v
```

Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/python/main.py backend/src/test/python/test_integration.py
git commit -m "feat: add main.py FastAPI entry point and integration tests"
```

---

### Task 10: 最终验证 + 清理

- [ ] **Step 1: 运行全部测试确认**

```bash
cd backend && .venv/Scripts/python.exe -m pytest src/test/python/ -v
```

- [ ] **Step 2: 确认文件清单完整**

```bash
ls -la backend/src/main/python/
ls -la backend/src/main/python/agents/
ls -la backend/src/main/python/models/
ls -la backend/src/main/python/prompts/
ls -la backend/src/main/python/routers/
ls -la backend/src/main/python/services/
ls -la backend/src/test/python/
```

- [ ] **Step 3: 若有遗漏则补充提交**
