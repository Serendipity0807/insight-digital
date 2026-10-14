package com.insight.product.service;

import com.insight.product.dto.ProductVO;
import org.springframework.stereotype.Service;

/**
 * 商品业务逻辑层。
 * Controller 只负责"接请求、转结果",真正的业务逻辑在 Service。
 * 下一课:这里改成从数据库查;现在先返回假数据。
 */
@Service
public class ProductService {

    /** 按 id 查询商品详情 */
    public ProductVO detail(Long id) {
        // 假数据:下一课换成查库
        return new ProductVO(id, "iPhone 15 Pro 256G 原色钛金属", 699900);
    }
}
