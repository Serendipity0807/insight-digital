package com.insight.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SPU 实体:对应 spu 表(一个"商品")。
 */
@Data
@TableName("spu")
public class Spu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Long brandId;

    private Long categoryId;

    private String mainImage;

    private String description;

    private Integer sales;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
