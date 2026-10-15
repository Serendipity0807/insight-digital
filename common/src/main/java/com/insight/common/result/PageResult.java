package com.insight.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果:所有分页接口的响应骨架。
 * 注意:common 是纯基础设施模块,不依赖 MyBatis-Plus,
 * 所以这里只放"结果",不放"查询逻辑"——IPage 的转换在业务 service 里做。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 总条数(不是总页数) */
    private long total;

    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
