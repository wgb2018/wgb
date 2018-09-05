package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Notice;
import com.microdev.param.*;

public interface NoticeService extends IService<Notice> {

    ResultDO createNotice(CreateNoticeRequest request);

    ResultDO queryNotice(Paginator paginator, QueryNoticeRequest Request);

    ResultDO myselfNotice(Paginator paginator, QueryNoticeRequest Request);

    ResultDO acceptNotice(AcceptNoticeRequest request);

    ResultDO recommendtNotice(QueryNoticeRequest request);

    ResultDO detailsNotice(QueryNoticeRequest request);

    ResultDO detailsAccept(Paginator paginator, QueryNoticeRequest request);

    ResultDO noticeHandle(NoticeHandleParam request);

    ResultDO enrollHandle(Paginator paginator, QueryNoticeRequest request);

    ResultDO recommendWorker(String id);


}
