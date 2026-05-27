import json
from agents.base import create_agent, load_prompt


class PushAgent:
    def __init__(self):
        self.system_message = load_prompt("push.md")
        self._agent = create_agent("push_agent", self.system_message)

    async def run(self, health_context: str) -> str:
        result = await self._agent.run(task=health_context)
        content = result.messages[-1].content
        return content

    @staticmethod
    def parse_response(raw: str) -> dict:
        text = raw.strip()
        if text.startswith("```"):
            lines = text.split("\n")
            lines = [l for l in lines if not l.startswith("```")]
            text = "\n".join(lines)
        return json.loads(text)
