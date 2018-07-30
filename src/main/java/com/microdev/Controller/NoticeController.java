package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.CreateNoticeRequest;
import com.microdev.param.CreateSuggestionRequest;
import com.microdev.param.QueryNoticeRequest;
import com.microdev.param.SuggestionQuery;
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
    @PostMapping("/hotel/release/notice")
    public ResultDO createHotelNotice(@RequestBody CreateNoticeRequest request) {
        return noticeService.createNotice(request);
    }
    /**
     * 查询
     */
    @PostMapping("/query/notice")
    public ResultDO queryNotice(@RequestBody PagingDO<QueryNoticeRequest> paging) {
        return noticeService.queryNotice(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 人力发布招聘公告
     */
    @PostMapping("/hr/release/notice")
    public ResultDO createHrNotice(@RequestBody CreateNoticeRequest request) {
        return noticeService.createNotice(request);
    }
    /**
     * 公告报名
     */
    @PostMapping("/accept/notice")
    public ResultDO acceptNotice(@RequestBody CreateNoticeRequest request) {
        return noticeService.createNotice(request);
    }

}
