import os
from pathlib import Path
from dotenv import load_dotenv


def _find_and_load_env() -> None:
    current = Path(__file__).resolve().parent
    for parent in [current] + list(current.parents):
        env_file = parent / ".env"
        if env_file.is_file():
            load_dotenv(env_file)
            return


_find_and_load_env()


class Settings:
    AI_API_KEY: str = os.getenv("AI_API_KEY", "")
    AI_API_BASE_URL: str = os.getenv("AI_API_BASE_URL", "https://api.openai.com/v1")
    AI_MODEL: str = os.getenv("AI_MODEL", "gpt-4o-mini")


settings = Settings()
