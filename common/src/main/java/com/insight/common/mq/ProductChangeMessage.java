package com.insight.common.mq;

import com.insight.common.search.EsProductDoc;
import lombok.Data;

/**
 * 商品变更消息:mall-product 数据一变就发到 product-change topic,
 * mall-search 消费后增量更新 ES。契约类放 common = 协议只有一份定义。
 */
@Data
public class ProductChangeMessage {

    /** 新增 / 上架 / 将来改价 → ES 覆盖写(幂等:_id 相同就是覆盖) */
    public static final String TYPE_UPSERT = "UPSERT";

    /** 下架 / 逻辑删除 → ES 删文档 */
    public static final String TYPE_DELETE = "DELETE";

    private String type;

    private Long spuId;

    /** UPSERT 时带上完整文档;DELETE 时为 null */
    private EsProductDoc doc;
}
