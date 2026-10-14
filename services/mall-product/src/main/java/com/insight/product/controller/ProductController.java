package com.insight.product.controller;

import com.insight.common.result.Result;
import com.insight.product.dto.ProductDetailVO;
import com.insight.product.service.ProductService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /** 按 id 查询商品详情。GET http://localhost:8082/api/v1/product/1001 */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(
            @PathVariable("id") @Positive(message = "商品ID必须为正数") Long id) {
        return Result.success(productService.detail(id));
    }
}
