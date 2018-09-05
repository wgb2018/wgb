package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.*;
import com.microdev.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    /**
     * 用人单位发布公告
     */
    @PostMapping("/hotel/release/notice")
    public ResultDO createHotelNotice(@RequestBody CreateNoticeRequest request) {
        request.setType (1);
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
     * 公告管理
     */
    @PostMapping("/myself/notice")
    public ResultDO myselfNotice(@RequestBody PagingDO<QueryNoticeRequest> paging) {
        return noticeService.myselfNotice(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 人力发布招聘公告
     */
    @PostMapping("/hr/release/notice")
    public ResultDO createHrNotice(@RequestBody CreateNoticeRequest request) {
        request.setType (2);
        return noticeService.createNotice(request);
    }
    /**
     * 公告报名
     */
    @PostMapping("/accept/notice")
    public ResultDO acceptNotice(@RequestBody AcceptNoticeRequest request) {
        return noticeService.acceptNotice(request);
    }
    /**
     * 公告推荐
     */
    @PostMapping("/recommend/notice")
    public ResultDO recommendNotice(@RequestBody QueryNoticeRequest request) {
        return noticeService.recommendtNotice(request);
    }
    /**
     * 公告详情
     */
    @PostMapping("/details/notice")
    public ResultDO detailsNotice(@RequestBody QueryNoticeRequest request) {
        return noticeService.detailsNotice(request);
    }
    /**
     * 报名情况
     */
    @PostMapping("/details/accept/all")
    public ResultDO detailsAcceptAll(@RequestBody PagingDO<QueryNoticeRequest> paging) {
        return noticeService.detailsAccept(paging.getPaginator(), paging.getSelector());
    }
    /**
     * 公告处理
     */
    @PostMapping("/notice/handle")
    public ResultDO noticeHandle(@RequestBody NoticeHandleParam request) {
        return noticeService.noticeHandle(request);
    }
    /**
     * 小时工报名管理
     */
    @PostMapping("/worker/enroll/handle")
    public ResultDO workerEnrollHandle(@RequestBody PagingDO<QueryNoticeRequest> request) {
        request.getSelector ( ).setType ("1");
        return noticeService.enrollHandle(request.getPaginator(), request.getSelector());
    }
    /**
     * 人力报名管理
     */
    @PostMapping("/hr/enroll/handle")
    public ResultDO hrEnrollHandle(@RequestBody PagingDO<QueryNoticeRequest> request) {
        request.getSelector ( ).setType ("2");
        return noticeService.enrollHandle(request.getPaginator(), request.getSelector());
    }
    /**
     * 酒店广场推荐小时工
     */
    @GetMapping("/hr/recommend/worker/{id}")
    public ResultDO hrRecommendWorker(@PathVariable String id) {
        return noticeService.recommendWorker(id);
    }


}
