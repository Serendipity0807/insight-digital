"""客服工具:慧眼数码服务政策问答(内置静态知识库)。"""
import json
import logging

from langchain_core.tools import tool
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)

# 服务政策知识库:量小,内置在代码里。
# 知识量变大后的升级路线:RAG 向量库检索
FAQ_ITEMS = [
    {
        "title": "退货政策",
        "answer": "支持 7 天无理由退货:商品完好、不影响二次销售即可申请。"
                  "流程:我的订单→申请退货→按提示寄回→仓库验收后 1-3 个工作日原路退款。"
                  "定制类、已拆封的耗材类商品不支持无理由退货。",
    },
    {
        "title": "发货与物流",
        "answer": "现货商品 48 小时内发货,默认顺丰/京东物流,"
                  "全国大部分地区 2-4 天送达。发货后可在'我的订单'查看物流单号与轨迹。",
    },
    {
        "title": "支付方式",
        "answer": "支持微信支付、支付宝、银行卡支付。下单后 10 分钟内未支付订单会自动关闭,"
                  "如需保留请尽快完成支付。",
    },
    {
        "title": "保修政策",
        "answer": "数码产品享一年官方质保:非人为损坏免费维修。"
                  "保修期内联系在线客服提供订单号即可申请,来回运费由平台承担。",
    },
    {
        "title": "发票",
        "answer": "支持开具电子发票,下单时勾选'需要发票'并填写抬头即可,"
                  "发票在确认收货后 7 个工作日内发送至订单邮箱。",
    },
    {
        "title": "优惠券使用规则",
        "answer": "优惠券在结算页自动抵扣,每单限用一张,不与其他满减活动叠加;"
                  "优惠券过期未用不补发。",
    },
]

class FaqArgs(BaseModel):
    question: str = Field(description="用户咨询的服务问题,例如'怎么退货'、'多久发货'")

@tool(args_schema=FaqArgs)
def answer_faq(question: str) -> str:
    """
    查询慧眼数码的服务政策:退货、发货、支付、保修、发票、优惠券规则。

    适用场景:用户咨询售后服务、物流时效、支付方式、保修政策等政策类问题。
    注意:某个具体订单的进度(发货了没、到哪了)不在这里查,需要调用订单查询工具。
    """
    logger.info("调用工具 answer_faq:question=%s", question)
    return json.dumps(FAQ_ITEMS, ensure_ascii=False)