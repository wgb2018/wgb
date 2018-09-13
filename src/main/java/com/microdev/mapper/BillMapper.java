package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Bill;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.param.PayRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillMapper extends BaseMapper<Bill> {
    List<Bill> selectHotelPayBill(HotelPayHrCompanyRequest request);

    List<Bill> selectHotelPayBillWorker(HotelPayHrCompanyRequest request);

    List<Bill> selectHrCompanyPayBill(HrCompanyPayWorkerRequest request);

    Integer selectHrCompanyPayBillCount(HrCompanyPayWorkerRequest request);

    List<PayRecord> selectHrCompanyPayBillRecord(HrCompanyPayWorkerRequest request);

    Integer selectHotelPayBillCount(HotelPayHrCompanyRequest request);

    List<PayRecord> selectHotelPayBillRecord(HotelPayHrCompanyRequest request);

    Integer selectHotelPayBillWorkerCount(HotelPayHrCompanyRequest request);

    List<PayRecord> selectHotelPayBillWorkerRecord(HotelPayHrCompanyRequest request);
}
