package com.insight.cart.client;

import com.insight.cart.dto.ProductSkuVO;
import com.insight.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** mall-product 的 Feign 客户端:name 填 Nacos 服务名,自动服务发现 + 负载均衡 */
@FeignClient(name = "mall-product")
public interface ProductClient {

    /** 批量查 SKU 摘要。契约:路径/参数/响应结构必须与 mall-product 对齐 */
    @GetMapping("/api/v1/product/sku/batch")
    Result<List<ProductSkuVO>> listSkuByIds(@RequestParam("ids") List<Long> ids);
}
