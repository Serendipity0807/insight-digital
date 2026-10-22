"""Agent 核心:组装 LLM、系统提示词与工具。api.py 只依赖这里的 answer()。"""
from langchain.agents import create_agent
from langchain_openai import ChatOpenAI

from app.config import settings
from app.core.tools.faq_tools import answer_faq
from app.core.tools.order_tools import get_my_order, list_my_orders
from app.core.tools.product_tools import get_product_detail
from app.core.tools.search_tools import search_products

llm = ChatOpenAI(
    api_key=settings.deepseek_api_key,
    base_url=settings.deepseek_base_url,
    model=settings.deepseek_model,
)

SYSTEM_PROMPT = (
    "你是慧眼数码(Insight Digital)电商平台的统一智能助手,能胜任四种角色:"
    "1. 客服:解答购物政策(退货、物流、支付、保修、发票、优惠券),查询用户订单;"
    "2. 导购:帮用户找商品、比价格、看详情;"
    "3. 数据:基于用户订单做统计(如'我这个月花了多少钱');"
    "4. 内容:撰写商品文案、营销文案。"
    "根据用户问题自动判断扮演哪个角色,不需要向用户说明。"
    "涉及商品和订单的问题必须调用工具获取真实数据,不要凭记忆编造。"
    "下单、取消订单等操作你无法执行,请引导用户去商城页面操作。"
    "回答使用中文,简洁友好。"
)

# 工具清单:一个服务域一个文件。注意:只给"查"的工具,不给"下单/取消"——
# 危险操作 Agent 只建议不执行(人机协同铁律)
tools = [search_products, get_product_detail, answer_faq, list_my_orders, get_my_order]

agent = create_agent(llm, tools, system_prompt=SYSTEM_PROMPT)


def answer(message: str) -> str:
    """Agent 统一入口:上层只调用这一个函数,不关心内部实现"""
    result = agent.invoke({"messages": [{"role": "user", "content": message}]})
    return result["messages"][-1].content
