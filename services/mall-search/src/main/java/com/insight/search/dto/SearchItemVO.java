package com.insight.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** 搜索结果条目:字段名与 ES 索引一一对应(驼峰直配) */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchItemVO {
    private Long id;
    private String name;
    private String tagline;
    private String mainImage;
    private Long brandId;
    private Long categoryId;
    private Integer minPrice;
    private Integer sales;
    private String tag;
}