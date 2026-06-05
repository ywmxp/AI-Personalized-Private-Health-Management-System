import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))

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
