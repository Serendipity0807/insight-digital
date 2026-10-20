"""搜索工具:通过网关调用 mall-search,在商品库(ES)中搜索。"""
import json
import logging
from typing import Optional

import httpx
from langchain_core.tools import tool
from pydantic import BaseModel, Field

from app.config import settings

logger = logging.getLogger(__name__)

class SearchProductsArgs(BaseModel):
    keyword: str = Field(description="搜索关键词,例如'蓝牙耳机', '降噪'等")
    sort: str = Field(
        default="default",
        description="排序方式:default=综合相关度,sales=销量,"
                    "price_asc=价格从低到高,price_desc=价格从高到低,newest=最新",
    )
    min_price: Optional[int] = Field(
        default=None, description="价格下限,单位是分,例如 10000 表示 100 元"
    )
    max_price: Optional[int] = Field(
        default=None, description="价格上限,单位是分,例如 20000 表示 200 元"
    )
    size: int = Field(default=5, description="返回的商品条数,默认 5 条")

@tool(args_schema=SearchProductsArgs)
def search_products(
        keyword: str,
        sort: str,
        min_price: Optional[int] = None,
        max_price: Optional[int] = None,
        size: int = 5
) -> str:
    """
    在商品库中搜索商品,返回名称、宣传语、价格(分)、销量、标签等摘要列表。

    适用场景:用户想找某类商品('有没有蓝牙耳机')、按价格区间筛选、按销量或价格排序推荐。
    返回列表中每项的 id 字段,可交给 get_product_detail 查询完整详情。
    """
    logger.info(
        "调用工具 search_products:keyword=%s, sort=%s, min_price=%s, max_price=%s",
        keyword, sort, min_price, max_price,
    )
    params = {"keyword": keyword, "sort": sort, "page": 1, "size": size}
    if min_price is not None:
        params["minPrice"] = min_price
    if max_price is not None:
        params["maxPrice"] = max_price
    try:
        resp = httpx.get(
            f"{settings.gateway_base_url}/api/v1/search",
            params=params,
            timeout=5.0,
        )
        resp.raise_for_status()
        body = resp.json()
        if body.get("code") != 200:
            return f"搜索失败:{body.get('message')}"
        records = body["data"]["records"]
        if not records:
            return f"没有找到与「{keyword}」相关的商品"
        return json.dumps(records, ensure_ascii=False)
    except Exception as e:
        return f"搜索商品时出错:{e},请稍后重试"