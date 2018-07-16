package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.param.CreateSuggestionRequest;
import com.microdev.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    /**
     * 酒店发布公告
     */
    @PostMapping("/release/notice")
    public ResultDO createSuggestion(@RequestBody CreateSuggestionRequest request) {
        return null;
    }
}
