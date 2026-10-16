package com.insight.order.client;

import com.insight.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** mall-product 的 Feign 客户端:name 填 Nacos 服务名 */
@FeignClient(name = "mall-product")
public interface ProductClient {

    @GetMapping("/api/v1/product/sku/batch")
    Result<List<ProductSkuVO>> listSkuByIds(@RequestParam("ids") List<Long> ids);
}
