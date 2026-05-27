from fastapi import FastAPI
from routers.ai import router as ai_router

app = FastAPI(title="AI Health Analysis Service", version="0.1.0")
app.include_router(ai_router)


@app.get("/health")
async def health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    from config import settings

    uvicorn.run(app, host=settings.AI_SERVICE_HOST, port=settings.AI_SERVICE_PORT)
