import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))

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
