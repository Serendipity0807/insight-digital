package com.insight.promotion.mq;

import lombok.Data;

/**
 * 秒杀削峰消息。服务内自产自销(seckill-order:promotion → promotion),
 * 所以不放 common——只有跨服务的契约才进 common。
 * 带上下单瞬间的价格快照:消费端不再读活动表,防止"排队期间活动改价"影响已下单的人。
 */
@Data
public class SeckillOrderMessage {

    private String requestId;

    private Long sessionId;

    private Long itemId;

    private Long userId;

    private Long skuId;

    /** 下单瞬间的秒杀价(分),快照 */
    private Integer seckillPrice;
}
