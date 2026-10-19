package com.insight.promotion.controller;

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

    /** 场次商品列表(公开)。GET /api/v1/promotion/seckill/{sessionId}/goods
     *  商品名/图片前端拿 skuId 调 /api/v1/product/sku/batch 组装 */
    @GetMapping("/{sessionId}/goods")
    public Result<List<SeckillGoodsVO>> listGoods(
            @PathVariable("sessionId") @Positive(message = "场次ID必须为正数") Long sessionId) {
        return Result.success(seckillService.listGoods(sessionId));
    }

    /** 秒杀下单(需登录):立即返回"排队中"+ requestId,重活进 MQ。
     *  userId 来自网关验签后透传的 X-User-Id——身份以网关为准,不信客户端自己报的 id */
    @PostMapping("/{sessionId}/order")
    public Result<SeckillOrderResponseVO> order(
            @PathVariable("sessionId") @Positive(message = "场次ID必须为正数") Long sessionId,
            @RequestBody @Valid SeckillOrderRequest req,
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(seckillService.order(sessionId, req.getItemId(), userId));
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
