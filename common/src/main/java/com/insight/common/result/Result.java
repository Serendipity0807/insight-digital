package com.insight.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体:所有服务所有接口,返回给前端/网关的结构都是它。
 * :{ code, message, data }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    /** 成功码 */
    public static final int SUCCESS_CODE = 200;

    private int code;
    private String message;
    private T data;

    /** 成功,带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    /** 成功,无数据 */
    public static <T> Result<T> success() {
        return success(null);
    }

    /** 失败:错误码 + 提示 */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
