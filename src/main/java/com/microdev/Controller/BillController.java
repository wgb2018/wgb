package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.BillRequest;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.param.HrQueryWorkerDTO;
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
    @PostMapping("/hotel/bill")
    public ResultDO hotelbill(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getHotelBill(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 人力公司按酒店查询账目
     *
     */
    @PostMapping("/hrCompany/bill/hotel")
    public ResultDO hrCompanybillhotel(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getCompanyBillHotel(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 人力公司按小时工查询账目
     *
     */
    @PostMapping("/hrCompany/bill/worker")
    public ResultDO hrCompanybillworker(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getCompanyBillWorker(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 小时工按人力公司工查询账目
     *
     */
    @PostMapping("/worker/bill")
    public ResultDO workerbill(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getWorkerBill(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 查询酒店支付人力公司记录
     */
    @PostMapping("/HotelPayHrCompany/Bill")
    public ResultDO queryHotelPayHrCompany(@RequestBody PagingDO<HotelPayHrCompanyRequest> paging) {
        return BillService.queryHotelPayHrCompany(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 查询人力公司支付小时工记录
     */
    @PostMapping("/HrCompanyPayWorker/Bill")
    public ResultDO queryHrCompanyPayWorker(@RequestBody PagingDO<HrCompanyPayWorkerRequest> paging) {
        return BillService.queryHrCompanyPayWorker(paging.getPaginator (),paging.getSelector ());
    }
}
