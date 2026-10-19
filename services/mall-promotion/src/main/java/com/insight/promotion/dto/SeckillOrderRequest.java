package com.insight.promotion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 秒杀下单请求体:只收 itemId——场次在 URL、价格/商品由服务端从库里取,不信客户端 */
@Data
public class SeckillOrderRequest {

    @NotNull(message = "itemId 不能为空")
    @Positive(message = "itemId 必须为正数")
    private Long itemId;
}
