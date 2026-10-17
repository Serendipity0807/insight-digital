package com.insight.inventory.controller;

import com.insight.common.result.Result;
import com.insight.inventory.dto.DeductRequest;
import com.insight.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 服务间内部接口:不经网关,Feign 直连 */
@RestController
@RequestMapping("/internal/inventory")
public class InternalInventoryController {

    @Autowired
    private InventoryService inventoryService;

    /** 支付成功扣减锁定库存:由订单服务 Feign 调用 */
    @PostMapping("/deduct")
    public Result<Void> deduct(@RequestBody @Valid DeductRequest req) {
        inventoryService.deduct(req);
        return Result.success();
    }
}
