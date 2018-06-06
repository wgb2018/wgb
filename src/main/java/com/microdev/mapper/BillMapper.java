package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillMapper extends BaseMapper<Bill> {
    List<Bill> selectHotelPayBill(HotelPayHrCompanyRequest request);

    List<Bill> selectHrCompanyPayBill(HrCompanyPayWorkerRequest request);
}
