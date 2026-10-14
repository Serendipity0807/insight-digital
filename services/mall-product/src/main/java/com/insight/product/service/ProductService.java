package com.insight.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.insight.product.dto.ProductDetailVO;
import com.insight.product.entity.Sku;
import com.insight.product.entity.Spu;
import com.insight.product.mapper.SkuMapper;
import com.insight.product.mapper.SpuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    /** 按 id 查询商品详情(SPU + 该 SPU 下所有 SKU) */
    public ProductDetailVO detail(Long id) {
        // 1. 查 SPU
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return null; // 不存在,由 Controller 转成错误响应
        }
        // 2. 查该 SPU 下的所有 SKU(类型安全的条件构造器)
        List<Sku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id)
        );
        // 3. Entity 组装成 VO
        List<ProductDetailVO.SkuVO> skuVOs = skus.stream()
                .map(s -> new ProductDetailVO.SkuVO(
                        s.getId(), s.getSpec(), s.getPrice(), s.getOriginalPrice()))
                .collect(Collectors.toList());

        return new ProductDetailVO(
                spu.getId(), spu.getName(), spu.getMainImage(),
                spu.getDescription(), spu.getSales(), skuVOs);
    }
}
