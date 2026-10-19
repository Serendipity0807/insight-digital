package com.insight.notify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 发短信请求(服务间接口入参) */
@Data
public class SmsRequest {

    /** 手机号:11 位数字(学习项目不接真实短信服务,格式校验要有) */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "场景不能为空")
    @Size(max = 20, message = "场景过长")
    private String scene;

    @NotBlank(message = "短信内容不能为空")
    @Size(max = 500, message = "短信内容过长")
    private String content;
}
