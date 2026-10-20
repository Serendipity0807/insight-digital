from langchain.agents import create_agent
from langchain_openai import ChatOpenAI

from app.config import settings
from app.core.tools.product_tools import get_product_detail
from app.core.tools.search_tools import search_products

llm = ChatOpenAI(
    api_key = settings.deepseek_api_key,
    base_url = settings.deepseek_base_url,
    model = settings.deepseek_model,
)

SYSTEM_PROMPT = (
    "你是慧眼数码(Insight Digital)电商平台的智能助手。"
    "你熟悉电商购物场景,可以和用户聊天、解答问题。"
    "被问到超出能力范围的问题时,诚实说明,不要编造。"
    "回答使用中文,简洁友好。"
)

# Agent 的工具箱:一个微服务域一个文件,将来加订单工具 = 加 order_tools.py
tools = [get_product_detail, search_products]

agent = create_agent(llm, tools=tools, system_prompt=SYSTEM_PROMPT)

def answer(message: str) -> str:
    """Agent 统一入口:上层只调用这一个函数,不关心内部实现"""
    result = agent.invoke({"messages": [{"role": "user", "content": message}]})
    return result["messages"][-1].content