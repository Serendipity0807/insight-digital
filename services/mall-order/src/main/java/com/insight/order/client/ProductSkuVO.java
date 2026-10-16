package com.insight.order.client;

import lombok.Data;

/**
 * 商品服务 SKU 契约镜像:字段名必须与 mall-product 的 SkuVO 完全一致。
 * 注意:SKU 继承 SPU 的名字,所以商品名就是 spuName(哪怕名字怪,照抄)。
 */
@Data
public class ProductSkuVO {
    private Long id;
    private Long spuId;
    private String spec;
    private Integer price;
    private Integer originalPrice;
    private String image;
    private String spuName;
}
