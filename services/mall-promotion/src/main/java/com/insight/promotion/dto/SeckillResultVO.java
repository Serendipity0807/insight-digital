package com.insight.promotion.dto;

import lombok.Data;

/**
 * 秒杀结果快照:存在 Redis seckill:result:{requestId} 里(30 分钟过期)。
 * 下单写"排队中",消费端改写为终态,轮询接口读它。
 */
@Data
public class SeckillResultVO {

    public static final String STATUS_QUEUING = "QUEUING";    // 排队中
    public static final String STATUS_SUCCESS = "SUCCESS";    // 已抢到
    public static final String STATUS_SOLD_OUT = "SOLD_OUT";  // 已售罄
    public static final String STATUS_FAILED = "FAILED";      // 失败(未预热等,reason 给原因)

    /** 谁取的号:轮询时校验,防止拿别人的 requestId 偷看结果 */
    private Long userId;

    private String status;

    /** SUCCESS 时的秒杀订单 id */
    private Long orderId;

    /** FAILED 时的人话原因 */
    private String reason;
}
