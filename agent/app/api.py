from fastapi import APIRouter, Header
from pydantic import BaseModel, Field

from app.core.agent import answer
from app.core.context import current_token

router = APIRouter(prefix="/api/agent")


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000)


class ChatResponse(BaseModel):
    reply: str


@router.post("/chat", response_model=ChatResponse)
def chat(
    req: ChatRequest,
    authorization: str | None = Header(default=None),
) -> ChatResponse:
    # 前端像调网关一样带 Bearer token;Agent 只透传不验证(验证是网关的职责)
    token = None
    if authorization and authorization.startswith("Bearer "):
        token = authorization[7:]
    current_token.set(token)
    return ChatResponse(reply=answer(req.message))
