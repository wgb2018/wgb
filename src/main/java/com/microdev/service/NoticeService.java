package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.Notice;
import com.microdev.param.CreateNoticeRequest;

public interface NoticeService extends IService<Notice> {

    ResultDO createNotice(CreateNoticeRequest request);
}
