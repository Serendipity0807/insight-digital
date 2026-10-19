package com.insight.promotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 场次商品:商品名/图片前端拿 skuId 调 product 的 /sku/batch 组装(跨服务数据由前端聚合) */
@Data
@AllArgsConstructor
public class SeckillGoodsVO {

    private Long itemId;

    private Long skuId;

    /** 秒杀价(分) */
    private Integer seckillPrice;

    /** 活动库存(④-1b 预热后这里会换成 Redis 的实时剩余) */
    private Integer stock;
}
