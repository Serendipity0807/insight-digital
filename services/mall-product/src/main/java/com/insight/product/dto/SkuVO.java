package com.insight.product.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU 摘要 VO:供内部服务(购物车/订单)批量组装用。
 * 冗余了商品名 spuName——消费方不用再查一次 SPU。
 */
@Data
@NoArgsConstructor
public class SkuVO {
    private Long id;
    private Long spuId;
    private String spec;
    private Integer price;
    private Integer originalPrice;
    private String image;
    private String spuName;
}
