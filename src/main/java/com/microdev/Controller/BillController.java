package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.service.BillService;
import com.microdev.service.TaskHrCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 账目明细
 */
@RestController
public class BillController {
    @Autowired
    TaskHrCompanyService taskHrCompanyService;
    @Autowired
    BillService BillService;
    /**
     * 酒店按人力公司查询账目
     *
     */
    @GetMapping("/hotel/bill/{HotelId}")
    public ResultDO hotelbill(@PathVariable String HotelId) {
        return taskHrCompanyService.getHotelBill(HotelId);
    }
    /**
     * 人力公司按酒店查询账目
     *
     */
    @GetMapping("/hrCompany/bill/{HrCompanylId}/hotel")
    public ResultDO hrCompanybillhotel(@PathVariable String HrCompanylId) {
        return taskHrCompanyService.getCompanyBillHotel(HrCompanylId);
    }
    /**
     * 人力公司按小时工查询账目
     *
     */
    @GetMapping("/hrCompany/bill/{HrCompanylId}/worker")
    public ResultDO hrCompanybillworker(@PathVariable String HrCompanylId) {
        return taskHrCompanyService.getCompanyBillWorker(HrCompanylId);
    }
    /**
     * 小时工按人力公司工查询账目
     *
     */
    @GetMapping("/worker/bill/{workerId}")
    public ResultDO workerbill(@PathVariable String workerId) {
        return taskHrCompanyService.getWorkerBill(workerId);
    }
    /**
     * 查询酒店支付人力公司记录
     */
    @PostMapping("/HotelPayHrCompany/Bill")
    public ResultDO queryHotelPayHrCompany(@RequestBody HotelPayHrCompanyRequest request) {
        return BillService.queryHotelPayHrCompany(request);
    }
    /**
     * 查询人力公司支付小时工记录
     */
    @PostMapping("/HrCompanyPayWorker/Bill")
    public ResultDO queryHrCompanyPayWorker(@RequestBody HrCompanyPayWorkerRequest request) {
        return BillService.queryHrCompanyPayWorker(request);
    }
}
