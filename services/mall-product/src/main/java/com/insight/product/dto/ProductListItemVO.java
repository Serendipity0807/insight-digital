package com.insight.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品列表项 VO:首页/列表页卡片需要的最小字段集。
 * minPrice = 该 SPU 下所有在售 SKU 的最低价(单位:分)。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemVO {

    private Long id;
    private String name;
    private String mainImage;
    private Integer sales;
    private Integer minPrice;
    private String tag;      // 慧眼点评标签
    private String tagline;  // 一句话卖点
    private Long minSkuId;   // 最低价对应的 SKU id:卡片"加入购物车"直接加这个 SKU

}
