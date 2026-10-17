package com.insight.cart.controller;

import com.insight.cart.dto.AddCartRequest;
import com.insight.cart.dto.CartItemVO;
import com.insight.cart.dto.UpdateCheckedRequest;
import com.insight.cart.dto.UpdateQuantityRequest;
import com.insight.cart.service.CartService;
import com.insight.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {


    @Autowired
    private CartService cartService;

    /** 添加商品。X-User-Id 由网关验签后注入,服务内直接信任 */
    @PostMapping("/items")
    public Result<Void> add(@RequestHeader("X-User-Id") Long userId,
                            @RequestBody @Valid AddCartRequest req) {
        cartService.add(userId, req.getSkuId(), req.getQuantity());
        return Result.success();
    }

    /** 购物车列表 */
    @GetMapping
    public Result<List<CartItemVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.list(userId));
    }

    /** 修改数量 */
    @PutMapping("/items/{skuId}")
    public Result<Void> updateQuantity(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable("skuId") @Positive(message = "商品规格必须为正数") Long skuId,
                                       @RequestBody @Valid UpdateQuantityRequest req) {
        cartService.updateQuantity(userId, skuId, req.getQuantity());
        return Result.success();
    }

    /** 勾选/取消勾选 */
    @PutMapping("/items/{skuId}/checked")
    public Result<Void> updateChecked(@RequestHeader("X-User-Id") Long userId,
                                      @PathVariable("skuId") @Positive(message = "商品规格必须为正数") Long skuId,
                                      @RequestBody @Valid UpdateCheckedRequest req) {
        cartService.updateChecked(userId, skuId, req.getChecked());
        return Result.success();
    }

    /** 删除条目 */
    @DeleteMapping("/items/{skuId}")
    public Result<Void> remove(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable("skuId") @Positive(message = "商品规格必须为正数") Long skuId) {
        cartService.remove(userId, skuId);
        return Result.success();
    }

    /** 健康探活 */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
