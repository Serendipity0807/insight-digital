package com.insight.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCheckedRequest {

    // 假设 skuId 通过 @PathVariable 在 URL 中传递，所以这里只校验选中状态
    @NotNull(message = "选中状态不能为空")
    private Boolean checked;
}