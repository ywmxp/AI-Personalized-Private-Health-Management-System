import os
from autogen_ext.models.openai import OpenAIChatCompletionClient
from autogen_agentchat.agents import AssistantAgent
from autogen_core.models import ModelInfo
from config import settings


def create_model_client() -> OpenAIChatCompletionClient:
    return OpenAIChatCompletionClient(
        model=settings.AI_MODEL,
        api_key=settings.AI_API_KEY,
        base_url=settings.AI_API_BASE_URL,
        model_info=ModelInfo(
            vision=False,
            function_calling=True,
            json_output=True,
            family="unknown",
            structured_output=False,
        ),
    )


def create_agent(name: str, system_message: str) -> AssistantAgent:
    client = create_model_client()
    return AssistantAgent(
        name=name,
        model_client=client,
        system_message=system_message,
    )


def load_prompt(filename: str) -> str:
    prompt_dir = os.path.join(os.path.dirname(__file__), "..", "prompts")
    with open(os.path.join(prompt_dir, filename), "r", encoding="utf-8") as f:
        return f.read()
