package com.insight.promotion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 秒杀下单响应:永远立即返回"排队中"+ 取号单,真实结果靠轮询 */
@Data
@AllArgsConstructor
public class SeckillOrderResponseVO {

    /** 取号单:轮询 result 接口的凭证 */
    private String requestId;

    /** 下单时刻固定是"排队中" */
    private String status;
}
