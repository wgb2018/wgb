package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Notice;
import com.microdev.param.AcceptNoticeRequest;
import com.microdev.param.CreateNoticeRequest;
import com.microdev.param.QueryNoticeRequest;

public interface NoticeService extends IService<Notice> {

    ResultDO createNotice(CreateNoticeRequest request);

    ResultDO queryNotice(Paginator paginator, QueryNoticeRequest Request);

    ResultDO acceptNotice(AcceptNoticeRequest request);

    ResultDO recommendtNotice(QueryNoticeRequest request);


}
