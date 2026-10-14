package com.insight.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品接口(第一版:返回写死的假数据,下一课接数据库)。
 */
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    /**
     * 按 id 查询商品详情。访问:GET http://localhost:8082/api/v1/product/1
     * @PathVariable 把 URL 里的 {id} 取出来绑定到参数 id。
     */
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable("id") Long id) {
        // 假数据:下一课换成数据库里的真实商品
        return Map.of(
                "id", id,
                "name", "iPhone 15 Pro 256G 原色钛金属",
                "price", 699900
        );
    }
}
