package com.insight.common.exception;

import lombok.Getter;

/**
 * 业务异常:Service 遇到"预期内的错误"(商品不存在、库存不足…)就抛它,
 * 由全局异常处理器统一转成 Result 返回。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
