package com.insight.inventory.controller;

import com.insight.common.result.Result;
import com.insight.inventory.dto.LockRequest;
import com.insight.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /** 锁定库存:内部接口,由订单服务 Feign 调用 */
    @PostMapping("/lock")
    public Result<Void> lock(@RequestBody @Valid LockRequest req) {
        inventoryService.lock(req);
        return Result.success();
    }

    /** 健康探活 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
