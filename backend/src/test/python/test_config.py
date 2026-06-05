import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent / "main" / "python"))


def test_settings_from_env(monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "sk-test-key")
    monkeypatch.setenv("AI_API_BASE_URL", "https://test.api.com/v1")
    monkeypatch.setenv("AI_MODEL", "gpt-4o")

    import config
    import importlib
    importlib.reload(config)

    assert config.settings.AI_API_KEY == "sk-test-key"
    assert config.settings.AI_API_BASE_URL == "https://test.api.com/v1"
    assert config.settings.AI_MODEL == "gpt-4o"


def test_settings_defaults(monkeypatch):
    monkeypatch.setenv("AI_API_KEY", "")
    monkeypatch.setenv("AI_API_BASE_URL", "")
    monkeypatch.setenv("AI_MODEL", "")

    import config
    import importlib
    importlib.reload(config)

    assert config.settings.AI_API_KEY == ""
    assert config.settings.AI_API_BASE_URL == ""
    assert config.settings.AI_MODEL == ""
