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
        context = (
            f"健康标签: {tags}\n"
            f"风险等级: {request.risk_level}\n"
            f"健康分析: {request.analysis}\n\n"
            f"候选知识:\n" + "\n".join(knowledge_lines)
        )
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
