package com.insight.search.service;

import com.insight.common.result.PageResult;
import com.insight.search.dto.HotWordVO;
import com.insight.search.dto.SearchItemVO;

import java.util.List;

public interface SearchService {
    PageResult<SearchItemVO> search(String keyword, Long categoryId, Long brandId,
                                    Integer minPrice, Integer maxPrice,
                                    String sort, int page, int size);

    /** 热门搜索词:对 tag 做词项聚合,按文档数取前 N */
    List<HotWordVO> hot(int size);

    /** 输入联想:对商品名做前缀查询,返回匹配的商品名列表 */
    List<String> suggest(String prefix);
}
