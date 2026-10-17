package com.insight.order.client;

import com.insight.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** mall-inventory 的 Feign 客户端 */
@FeignClient(name = "mall-inventory")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/lock")
    Result<Void> lock(@RequestBody LockRequest request);

    /** 支付成功扣减锁定库存(内部接口) */
    @PostMapping("/internal/inventory/deduct")
    Result<Void> deduct(@RequestBody DeductRequest request);

    /** 关单释放库存:locked 回补 saleable(内部接口) */
    @PostMapping("/internal/inventory/release")
    Result<Void> release(@RequestBody ReleaseRequest request);

}
