package com.insight.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 订单主表。表名是 orders:order 是 MySQL 保留字 */
@Data
@TableName("orders")
public class Order {

    /** 主键 = 订单号(雪花 ID) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    /** 商品总额(分) */
    private Integer totalAmount;
    /** 优惠金额(分) */
    private Integer discountAmount;
    /** 实付金额(分) = 总额 - 优惠 */
    private Integer payAmount;
    /** 状态机:0已取消 10待支付 20已支付 30已发货 40已完成 50退款中 60已退款 */
    private Integer status;
    /** 收货地址快照(JSON):下单那一刻的地址,以后改地址不影响历史订单 */
    private String addressSnapshot;
    private String remark;
    /** 支付截止时间:创建 +30 分钟,超时关单(阶段⑥ 延时消息) */
    private LocalDateTime expireTime;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
