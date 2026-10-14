package com.insight.common.exception;

import com.insight.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器:拦截所有 Controller 抛出的异常,统一转成 Result。
 * 为什么放 common:所有服务都要这一套,写一份大家共用。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 → 对应的业务错误码 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 兜底:其他没预料到的异常 → 通用错误 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        // TODO 下一课引入日志体系时,这里要打印异常堆栈
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
