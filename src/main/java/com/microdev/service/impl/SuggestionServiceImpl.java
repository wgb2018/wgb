package com.microdev.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.SuggestionMapper;
import com.microdev.model.Suggestion;
import com.microdev.param.CreateSuggestionRequest;
import com.microdev.param.SuggestionQuery;
import com.microdev.param.SuggestionResponse;
import com.microdev.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;


@Transactional
@Service
public class SuggestionServiceImpl extends ServiceImpl<SuggestionMapper,Suggestion> implements SuggestionService {
    @Autowired
    SuggestionMapper suggestionMapper;
    @Override
    public ResultDO createSuggestion(CreateSuggestionRequest request) {
        if (request.getSuggestionContent().isEmpty()) {
            throw new ParamsException("反馈意见不能为空");
        }
        if (request.getUserId().isEmpty()) {
            throw new ParamsException("反馈人不能为空");
        }
        Suggestion suggestion = new Suggestion();
        suggestion.setUserId(request.getUserId());
        suggestion.setSuggestionContent(request.getSuggestionContent());
        suggestionMapper.insert(suggestion);
        return ResultDO.buildSuccess("操作成功");
    }

    @Override
    public ResultDO getPageSuggestion(Paginator paginator, SuggestionQuery query) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<SuggestionResponse> list = suggestionMapper.querySuggestions(query);
        PageInfo<SuggestionResponse> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }
}
