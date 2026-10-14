package com.insight.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.insight.product.entity.Sku;

/** 继承 BaseMapper 即拥有全套 CRUD,无需写 SQL */
public interface SkuMapper extends BaseMapper<Sku> {
}
