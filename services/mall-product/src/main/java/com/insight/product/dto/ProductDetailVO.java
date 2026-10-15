package com.insight.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品详情 VO:SPU 信息 + 它的 SKU 列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

    private Long id;
    private String name;
    private String mainImage;
    private String description;
    private Integer sales;
    private String tag;      // 慧眼点评标签
    private String tagline;  // 一句话卖点
    private String specParams; // 参数表(JSON 字符串,前端解析)
    private List<SkuVO> skus;

    /** SKU 子结构 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuVO {
        private Long id;
        private String spec;
        private Integer price;
        private Integer originalPrice;
    }
}
