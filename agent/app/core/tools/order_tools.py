"""订单工具:通过网关调用 mall-order,查询当前登录用户的订单。

身份透传:工具不接收 userId 参数——身份来自 api.py 写入请求上下文的 token,
经网关验签转成 X-User-Id,订单服务只认网关给的身份。
"""
import json
import logging
from typing import Optional

import httpx
from langchain_core.tools import tool
from pydantic import BaseModel, Field

from app.config import settings
from app.core.context import current_headers

logger = logging.getLogger(__name__)


class ListOrdersArgs(BaseModel):
    status: Optional[int] = Field(
        default=None,
        description="订单状态过滤:0已取消 10待支付 20已支付 30已发货 "
                    "40已完成 50退款中 60已退款;不传表示查全部",
    )
    size: int = Field(default=5, description="返回的订单条数,默认 5 条")


@tool(args_schema=ListOrdersArgs)
def list_my_orders(status: Optional[int] = None, size: int = 5) -> str:
    """查询当前登录用户的订单列表(摘要:订单号、实付金额、状态、下单时间)。

    适用场景:用户问'我的订单有哪些'、'有没有待支付的订单'、'我这个月买了什么'。
    拿到订单号后,可用 get_my_order 查单个订单的明细。
    注意:用户未登录时调用会失败,此时提示用户先登录。
    """
    logger.info("调用工具 list_my_orders:status=%s, size=%s", status, size)
    params = {"page": 1, "size": size}
    if status is not None:
        params["status"] = status
    try:
        resp = httpx.get(
            f"{settings.gateway_base_url}/api/v1/order/page",
            params=params,
            headers=current_headers(),
            timeout=5.0,
        )
        resp.raise_for_status()
        body = resp.json()
        if body.get("code") != 200:
            return f"查询订单失败:{body.get('message')}"
        records = body["data"]["records"]
        if not records:
            return "你还没有相关订单"
        return json.dumps(records, ensure_ascii=False)
    except httpx.HTTPStatusError as e:
        if e.response.status_code == 401:
            return "用户未登录,请先登录后再查询订单"
        return f"查询订单时出错:{e},请稍后重试"
    except Exception as e:
        return f"查询订单时出错:{e},请稍后重试"


class OrderDetailArgs(BaseModel):
    order_id: int = Field(description="订单号(雪花 ID,一长串数字)")


@tool(args_schema=OrderDetailArgs)
def get_my_order(order_id: int) -> str:
    """查询当前登录用户的单个订单详情:金额、状态、收货地址、商品明细。

    适用场景:用户问某个订单里买了什么、多少钱、什么状态。
    只能查自己的订单:网关验 token,订单服务校验归属。
    """
    logger.info("调用工具 get_my_order:order_id=%s", order_id)
    try:
        resp = httpx.get(
            f"{settings.gateway_base_url}/api/v1/order/{order_id}",
            headers=current_headers(),
            timeout=5.0,
        )
        resp.raise_for_status()
        body = resp.json()
        if body.get("code") != 200:
            return f"查询订单失败:{body.get('message')}"
        if body.get("data") is None:
            return "没有找到这个订单"
        return json.dumps(body["data"], ensure_ascii=False)
    except httpx.HTTPStatusError as e:
        if e.response.status_code == 401:
            return "用户未登录,请先登录后再查询订单"
        return f"查询订单时出错:{e},请稍后重试"
    except Exception as e:
        return f"查询订单时出错:{e},请稍后重试"
