package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;


public interface BillService extends IService<Bill> {
    ResultDO queryHotelPayHrCompany(HotelPayHrCompanyRequest request);

    ResultDO queryHrCompanyPayWorker(HrCompanyPayWorkerRequest request);
}
