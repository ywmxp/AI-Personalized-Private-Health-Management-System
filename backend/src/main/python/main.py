import json
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from routers.ai import router as ai_router


class UTF8JSONResponse(JSONResponse):
    media_type = "application/json; charset=utf-8"

    def render(self, content) -> bytes:
        return json.dumps(content, ensure_ascii=False, separators=(",", ":")).encode("utf-8")


app = FastAPI(title="AI Health Analysis Service", version="0.1.0", default_response_class=UTF8JSONResponse)
app.include_router(ai_router)


@app.get("/health")
async def health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    from config import settings

    uvicorn.run(app, host=settings.AI_SERVICE_HOST, port=settings.AI_SERVICE_PORT)
