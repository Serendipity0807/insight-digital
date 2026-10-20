from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.core.agent import answer

router = APIRouter(prefix="/api/agent")

class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000)

class ChatResponse(BaseModel):
    reply: str

@router.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest) -> ChatResponse:
    return ChatResponse(reply = answer(req.message))