import sys
from pathlib import Path
from unittest.mock import AsyncMock

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))

import pytest


class MockAgentRun:
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

    original_init = ai_service.AIService.__init__

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
    monkeypatch.setenv("AI_API_KEY", "")
    import config
    import importlib
    importlib.reload(config)
    return config
