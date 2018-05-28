package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Suggestion;
import com.microdev.param.SuggestionQuery;
import com.microdev.param.SuggestionResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionMapper extends BaseMapper<Suggestion> {
    List<SuggestionResponse> querySuggestions(SuggestionQuery query);
}
