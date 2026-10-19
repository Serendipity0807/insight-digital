package com.insight.common.mq;

import lombok.Data;

/**
 * 订单事件契约:mall-order 发、mall-notify 收。
 * 放 common = 协议只有一份定义,谁改消息结构都要动这个类,发送/消费双方编译期同时感知。
 * 蓝图 topic:order-event(order → notify,下单/取消事件 → 通知)
 */
@Data
public class OrderEventMessage {

    public static final String TOPIC = "order-event";

    /** 下单成功 → 发站内信 */
    public static final String TYPE_CREATED = "CREATED";

    /** 取消/超时关单(下一课 notify ② 用,先占位) */
    public static final String TYPE_CANCELLED = "CANCELLED";

    /** 事件类型,见上面两个常量 */
    private String type;

    private Long orderId;

    /** 通知接收人 */
    private Long userId;

    /** 实付金额(分),通知文案用 */
    private Integer payAmount;
}
