"""商品工具:通过网关调用 mall-product,查询商品详情。"""
import json
import logging

import httpx
from langchain_core.tools import tool
from pydantic import BaseModel, Field

from app.config import settings

logger = logging.getLogger(__name__)

class ProductDetailArgs(BaseModel):
    product_id: int = Field(description="商品ID,为正整数,如'1001'")

@tool(args_schema=ProductDetailArgs)
def get_product_detail(product_id: int) -> str:
    """
    查询单个商品的完整详情:名称、宣传语、参数、可选 SKU 规格与价格。

    适用场景:用户问某个具体商品的详情、参数、有什么规格可选、卖多少钱。
    需要先知道商品 ID;不知道 ID 时,先用 search_products 搜索拿到 ID。
    """
    logger.info("调用工具 get_product_detail:product_id=%s", product_id)
    try:
        resp = httpx.get(
            f"{settings.gateway_base_url}/api/v1/product/{product_id}/",
            timeout=5.0,
        )
        resp.raise_for_status()
        body = resp.json()
        if body.get("code") != 200:
            return f"查询商品失败:{body.get('message')}"
        return json.dumps(body["data"], ensure_ascii=False)
    except Exception as e:
        return f"查询商品 {product_id} 时出错: {e},请稍后重试"