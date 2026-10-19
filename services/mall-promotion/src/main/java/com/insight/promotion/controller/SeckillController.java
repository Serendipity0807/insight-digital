package com.insight.promotion.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.insight.common.result.Result;
import com.insight.promotion.dto.SeckillGoodsVO;
import com.insight.promotion.dto.SeckillOrderRequest;
import com.insight.promotion.dto.SeckillOrderResponseVO;
import com.insight.promotion.dto.SeckillResultVO;
import com.insight.promotion.dto.SessionVO;
import com.insight.promotion.service.SeckillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/promotion/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /** 秒杀场次列表(公开)。GET /api/v1/promotion/seckill/session/list */
    @GetMapping("/session/list")
    public Result<List<SessionVO>> listSessions() {
        return Result.success(seckillService.listSessions());
    }

    /** 场次商品列表(公开)。GET /api/v1/promotion/seckill/{sessionId}/goods */
    @GetMapping("/{sessionId}/goods")
    public Result<List<SeckillGoodsVO>> listGoods(
            @PathVariable("sessionId") @Positive(message = "场次ID必须为正数") Long sessionId) {
        return Result.success(seckillService.listGoods(sessionId));
    }

    /** 秒杀下单(需登录):立即返回"排队中"+ requestId,重活进 MQ。
     *  @SentinelResource:稳定资源名,限流/熔断都按它配;
     *  被拦截时抛 BlockException → 走 blockHandler,不进业务方法。 */
    @PostMapping("/{sessionId}/order")
    @SentinelResource(value = "seckill-order", blockHandler = "orderBlockHandler")
    public Result<SeckillOrderResponseVO> order(
            @PathVariable("sessionId") @Positive(message = "场次ID必须为正数") Long sessionId,
            @RequestBody @Valid SeckillOrderRequest req,
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(seckillService.order(sessionId, req.getItemId(), userId));
    }

    /** 兜底出口:限流与熔断都走这里,按 BlockException 类型给不同文案。
     *  签名铁律——返回值与原方法一致、参数与原方法一致、末尾多一个 BlockException */
    public Result<SeckillOrderResponseVO> orderBlockHandler(
            Long sessionId, SeckillOrderRequest req, Long userId, BlockException e) {
        if (e instanceof DegradeException) {
            // 熔断:下游(Redis 等)出问题,服务自动快速失败
            return Result.error(429, "秒杀服务开小差了,请稍后再试");
        }
        // 限流(FlowException 等):门口排队,超出阈值的直接拒
        return Result.error(429, "排队的人太多,请稍后再试");
    }

    /** 秒杀结果轮询(需登录)。GET /api/v1/promotion/seckill/{sessionId}/result?requestId=xxx */
    @GetMapping("/{sessionId}/result")
    public Result<SeckillResultVO> result(
            @PathVariable("sessionId") @Positive(message = "场次ID必须为正数") Long sessionId,
            @RequestParam("requestId") @NotBlank(message = "requestId 不能为空") String requestId,
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(seckillService.result(sessionId, requestId, userId));
    }
}
