package com.insight.promotion.controller;

import com.insight.common.result.Result;
import com.insight.promotion.dto.SeckillGoodsVO;
import com.insight.promotion.dto.SessionVO;
import com.insight.promotion.service.SeckillService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
