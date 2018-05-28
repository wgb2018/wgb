package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.CreateSuggestionRequest;
import com.microdev.param.SuggestionQuery;
import com.microdev.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SuggestionController {
    @Autowired
    private SuggestionService suggestionService;

    /**
     * 新建意见反馈
     */
    @PostMapping("/suggestion")
    public ResultDO createSuggestion(@RequestBody CreateSuggestionRequest request) {
        return suggestionService.createSuggestion(request);
    }
    /**
     * 分页查询意见反馈
     */
    @PostMapping("/suggestion/search")
    public ResultDO getPageData(@RequestBody PagingDO<SuggestionQuery> paging) {
        return suggestionService.getPageSuggestion(paging.getPaginator(),paging.getSelector());
    }

}
