package com.insight.common.exception;

import lombok.Getter;

/**
 * 统一错误码:
 * 100xx 通用;102xx 商品;其余服务按分段表继续追加。
 */
@Getter
public enum ErrorCode {

    INTERNAL_ERROR(10000, "系统繁忙,请稍后再试"),
    PARAM_ERROR(10001, "参数错误"),

    PRODUCT_NOT_FOUND(10201, "商品不存在"),

    PHONE_ALREADY_REGISTERED(10301, "该手机号已注册"),
    LOGIN_FAILED(10302, "手机号或密码错误");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
