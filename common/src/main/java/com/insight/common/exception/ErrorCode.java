package com.insight.common.exception;

import lombok.Getter;

/**
 * 统一错误码分段表:
 * 100xx 通用;102xx 商品;103xx 用户;104xx 购物车;105xx 订单;106xx 库存;107xx 支付。
 */
@Getter
public enum ErrorCode {

    INTERNAL_ERROR(10000, "系统繁忙,请稍后再试"),

    PARAM_ERROR(10001, "参数错误"),

    PRODUCT_NOT_FOUND(10201, "商品不存在"),

    PHONE_ALREADY_REGISTERED(10301, "该手机号已注册"),

    LOGIN_FAILED(10302, "手机号或密码错误"),

    CART_ITEM_NOT_FOUND(10401, "购物车条目不存在"),

    ORDER_ITEM_INVALID(10501, "商品不存在或已下架"),

    ORDER_STATUS_INVALID(10502, "订单状态异常,请刷新重试"),

    STOCK_NOT_ENOUGH(10601, "库存不足"),

    STOCK_LOCK_CONFLICT(10602, "下单太火爆,请稍后再试"),

    STOCK_DEDUCT_CONFLICT(10603, "库存扣减冲突,请稍后再试"),

    STOCK_RELEASE_CONFLICT(10604, "库存释放冲突,请稍后再试"),

    ORDER_NOT_PAYABLE(10701, "订单不存在或不可支付"),

    PAYMENT_NOT_FOUND(10702, "支付单不存在");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
