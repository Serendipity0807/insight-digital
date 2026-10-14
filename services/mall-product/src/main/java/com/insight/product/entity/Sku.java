package com.insight.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SKU 实体:对应 sku 表(真正能买、有价格的规格)。
 */
@Data
@TableName("sku")
public class Sku {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long spuId;

    private String spec;

    private Integer price;

    private Integer originalPrice;

    private String image;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
