package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;


public interface BillService extends IService<Bill> {
    ResultDO queryHotelPayHrCompany(Paginator paginator, HotelPayHrCompanyRequest request);

    ResultDO queryHrCompanyPayWorker(Paginator paginator,HrCompanyPayWorkerRequest request);
}
