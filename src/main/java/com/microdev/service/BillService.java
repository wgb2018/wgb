package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;

import java.util.Map;


public interface BillService extends IService<Bill> {
    ResultDO queryHotelPayHrCompany(Paginator paginator, HotelPayHrCompanyRequest request);

    ResultDO queryHrCompanyPayWorker(Paginator paginator,HrCompanyPayWorkerRequest request);

    ResultDO updateCommentStatus(String id);

    Map<String, Object> queryWorkerMoneyRecord(HrCompanyPayWorkerRequest request);

    Map<String, Object> queryHrMoneyRecord(HotelPayHrCompanyRequest request);
}
