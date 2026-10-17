package com.insight.product.controller;

import com.insight.common.result.Result;
import com.insight.product.dto.ProductDetailVO;
import com.insight.product.dto.SkuVO;
import com.insight.product.service.ProductService;
import com.insight.common.result.PageResult;
import com.insight.product.dto.ProductListItemVO;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


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

    /** 分页查询商品列表。GET http://localhost:8082/api/v1/product/page?page=1&size=10 */
    @GetMapping("/page")
    public Result<PageResult<ProductListItemVO>> page(
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "页码必须从1开始") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "每页条数至少为1")
            @Max(value = 100, message = "每页条数不能超过100") int size,
            @RequestParam(value = "kw", required = false) String kw) {
        return Result.success(productService.page(page, size, kw));
    }

    /** 批量查 SKU 摘要。GET /api/v1/product/sku/batch?ids=10041,10042
     *  供内部服务(购物车等)组装展示用 */
    @GetMapping("/sku/batch")
    public Result<List<SkuVO>> listSkuByIds(
            @RequestParam("ids") @NotEmpty(message = "ids 不能为空")
            @Size(max = 100, message = "一次最多查 100 个") List<Long> ids) {
        return Result.success(productService.listSkuByIds(ids));
    }
}
