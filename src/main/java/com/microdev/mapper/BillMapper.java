package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import org.springframework.stereotype.Repository;

@Repository
public interface BillMapper extends BaseMapper<Bill> {
    Bill selectHotelPayBill(HotelPayHrCompanyRequest request);

    Bill selectHrCompanyPayBill(HrCompanyPayWorkerRequest request);
}
