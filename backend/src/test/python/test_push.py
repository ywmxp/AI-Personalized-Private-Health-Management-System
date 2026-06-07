import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))

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
