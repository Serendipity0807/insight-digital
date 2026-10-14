package com.insight.product.controller;

import com.insight.common.result.Result;
import com.insight.product.dto.ProductDetailVO;
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

    /** 按 id 查询商品详情。GET http://localhost:8082/api/v1/product/1001 */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable("id") Long id) {
        ProductDetailVO vo = productService.detail(id);
        if (vo == null) {
            // 10201 = 商品错误码段(见接口文档);下一课用全局异常统一管理
            return Result.error(10201, "商品不存在");
        }
        return Result.success(vo);
    }
}
