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
    risk_level: str = Field(..., alias="riskLevel")
    analysis: str = ""
    knowledge_items: list[KnowledgeItem] = Field(..., alias="knowledgeItems")


class PushResponse(BaseModel):
    selected_knowledge_ids: list[int] = Field(..., alias="selectedKnowledgeIds")
    reason: str
