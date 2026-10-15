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

    /** 慧眼点评标签(如"影像旗舰"),阶段⑦ 由内容 Agent 生成 */
    private String tag;

    /** 一句话卖点(如"A17 Pro 芯片 · 钛金属机身") */
    private String tagline;

    /** 参数表(技术参数,驱动参数可视化),JSON 字符串 */
    private String specParams;

}
