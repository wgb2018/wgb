package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.Enroll;
import com.microdev.param.AcceptNoticeRequest;

public interface EnrollService extends IService<Enroll> {
    /**
     * 人力申请报名酒店任务
     * @param request
     * @param
     * @return
     */
    ResultDO hrApplyRegistration(AcceptNoticeRequest request);
    /**
     * 小时工申请报名酒店任务
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyHotel(AcceptNoticeRequest request);
    /**
     * 小时工申请报名人力任务
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyHr(AcceptNoticeRequest request);
    /**
     * 小时工申请报名人力招聘
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyRegistration(AcceptNoticeRequest request);
}
