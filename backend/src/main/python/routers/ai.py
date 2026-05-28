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
