package com.insight.product.controller;

import com.insight.common.result.Result;
import com.insight.product.dto.ProductVO;
import com.insight.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /** 按 id 查询商品详情。GET http://localhost:8082/api/v1/product/1 */
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable("id") Long id) {
        return Result.success(productService.detail(id));
    }
}
