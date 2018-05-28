package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.mapper.BillMapper;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper,Bill> implements BillService{
    @Autowired
    BillMapper billMapper;
    /**
     * 酒店按人力公司查询支付记录
     */
    @Override
    public ResultDO queryHotelPayHrCompany(HotelPayHrCompanyRequest request) {
        return ResultDO.buildSuccess(billMapper.selectHotelPayBill(request));
    }

    @Override
    public ResultDO queryHrCompanyPayWorker(HrCompanyPayWorkerRequest request) {
        return ResultDO.buildSuccess(billMapper.selectHrCompanyPayBill(request));
    }
}
