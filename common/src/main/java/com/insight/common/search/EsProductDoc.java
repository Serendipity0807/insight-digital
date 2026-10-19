package com.insight.common.search;

import lombok.Data;

/**
 * ES 商品文档契约:mall-product 生产、mall-search 消费,两边共用。
 * 12 个字段与全量同步脚本 es-full-sync.py 写进 product 索引的文档一一对应——
 * 增量 upsert 和全量覆盖的是同一个结构,谁都不能偷偷加字段或改名。
 */
@Data
public class EsProductDoc {

    private Long id;

    private String name;

    private String tagline;

    private String description;

    private Long brandId;

    private Long categoryId;

    private Integer sales;

    private Integer status;

    private String tag;

    private String mainImage;

    /** ES mapping 里是 date 类型;全量脚本写入 "yyyy-MM-dd'T'HH:mm:ss" 格式,这里保持一致 */
    private String createdAt;

    /** 最低价:在售 SKU 的 MIN(price),单位分,口径与全量脚本一致 */
    private Integer minPrice;
}
