package com.insight.promotion.controller;

import com.insight.common.result.Result;
import com.insight.promotion.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务间内部接口:前缀 /internal/** 不被网关路由(网关只有 /api/v1/** 的规则),
 * 外部用户进不来——预热是运营侧动作,只配给定时任务/admin 服务(将来)调用。
 */
@RestController
@RequestMapping("/internal/promotion/seckill")
public class InternalSeckillController {

    @Autowired
    private SeckillService seckillService;

    /** 预热:把场次商品的活动库存抄进 Redis。POST /internal/promotion/seckill/{sessionId}/warmup */
    @PostMapping("/{sessionId}/warmup")
    public Result<Integer> warmup(@PathVariable("sessionId") Long sessionId) {
        return Result.success(seckillService.warmup(sessionId));
    }
}
