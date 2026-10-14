package com.insight.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品详情 VO。金额单位:分。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO implements Serializable {

    private Long id;
    private String name;
    private Integer price;
}
