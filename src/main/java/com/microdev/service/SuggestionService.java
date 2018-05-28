package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Suggestion;
import com.microdev.param.CreateSuggestionRequest;
import com.microdev.param.SuggestionQuery;

public interface SuggestionService extends IService<Suggestion> {
    /**
     * 创建
     */
    ResultDO createSuggestion(CreateSuggestionRequest request);

    ResultDO getPageSuggestion(Paginator paginator, SuggestionQuery query);
}
