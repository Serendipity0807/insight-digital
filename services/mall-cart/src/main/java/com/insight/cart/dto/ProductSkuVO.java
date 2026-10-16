package com.insight.cart.dto;

import lombok.Data;

/**
 * mall-product /sku/batch 返回结构的本地镜像。
 * 只声明用到的字段,Jackson 按字段名匹配,多余字段自动忽略。
 * 注意:product 改字段名 = 破坏契约,必须同步这里。
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
