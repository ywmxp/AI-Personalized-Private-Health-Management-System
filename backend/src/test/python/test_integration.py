import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))

import pytest
from fastapi import HTTPException
from fastapi.testclient import TestClient


@pytest.fixture
def client(mock_ai_service, monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "sk-test-key")
    import config
    import importlib
    importlib.reload(config)
    monkeypatch.setattr("routers.ai.get_ai_service", lambda: mock_ai_service)
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
