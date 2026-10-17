package com.insight.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 支付单:一单一支付单,payment_no 是幂等关键 */
@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 支付单号 = "PAY" + orderId,天然唯一(一单一支付单) */
    private String paymentNo;

    private Long orderId;

    private Long userId;

    /** 支付渠道:ALIPAY / WECHAT */
    private String channel;

    /** 支付金额(分):发起支付时从订单服务拿,不信客户端 */
    private Integer amount;

    /** 0待支付 1成功 2失败 3已退款 */
    private Integer status;

    /** 渠道流水号(回调时写入) */
    private String tradeNo;

    /** 回调时间 */
    private LocalDateTime notifyTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
