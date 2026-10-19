package com.insight.search.service;

import com.insight.common.result.PageResult;
import com.insight.search.dto.SearchItemVO;

public interface SearchService {
    PageResult<SearchItemVO> search(String keyword, Long categoryId, Long brandId,
                                    Integer minPrice, Integer maxPrice,
                                    String sort, int page, int size);
}
