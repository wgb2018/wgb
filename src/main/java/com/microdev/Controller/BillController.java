package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.param.*;
import com.microdev.service.BillService;
import com.microdev.service.TaskHrCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


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
     * 用人单位按人力公司查询账目
     *
     */
    @PostMapping("/hotel/bill")
    public ResultDO hotelbillByHr(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getHotelBill(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 用人单位按小时工查询账目
     *
     */
    @PostMapping("/hotel/bill/worker")
    public ResultDO hotelbillByWorker(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getHotelBillWorker(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 人力公司按用人单位查询账目
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
    @PostMapping("/worker/bill/hr")
    public ResultDO workerbill(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getWorkerBill(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 小时工按用人单位工查询账目
     *
     */
    @PostMapping("/worker/bill/hotel")
    public ResultDO workerbillHotel(@RequestBody PagingDO<BillRequest> paging) {
        return taskHrCompanyService.getWorkerBillHotel(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 查询用人单位支付人力公司记录
     */
    @PostMapping("/HotelPayHrCompany/Bill")
    public ResultDO queryHotelPayHrCompany(@RequestBody PagingDO<HotelPayHrCompanyRequest> paging) {
        return BillService.queryHotelPayHrCompany(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 查询用人单位支付小时工记录
     */
    @PostMapping("/HotelPayWorker/Bill")
    public ResultDO queryHotelPayWorker(@RequestBody PagingDO<HotelPayHrCompanyRequest> paging) {
        return BillService.queryHotelPayWorker(paging.getPaginator (),paging.getSelector ());
    }
    /**
     * 查询人力公司支付小时工记录
     */
    @PostMapping("/HrCompanyPayWorker/Bill")
    public ResultDO queryHrCompanyPayWorker(@RequestBody PagingDO<HrCompanyPayWorkerRequest> paging) {
        return BillService.queryHrCompanyPayWorker(paging.getPaginator (),paging.getSelector ());
    }

    /**
     * 关闭评论
     * @return
     */
    @PostMapping("/update/bill")
    public ResultDO updateBillComment(@RequestBody Map<String, String> param) {

        return BillService.updateCommentStatus(param.get("id"));
    }

    /**
     * 下载小时工的收益记录
     * @param request
     * @param response
     */
    @GetMapping("/worker/bill/download")
    public void downloadWorkerBillPay(@ModelAttribute HrCompanyPayWorkerRequest request, HttpServletResponse response) {
        Map<String, Object> param = BillService.queryWorkerMoneyRecord(request);
        ExcelUtil.download(response, (List<PayRecord>)param.get("list"), ExcelUtil.payRecord, "支付记录", param.get("name").toString() + "收款记录");
    }

    /**
     * 下载人力收益记录
     * @param request
     * @param response
     */
    @GetMapping("/hr/bill/download")
    public void downloadHrBillPay(@ModelAttribute HotelPayHrCompanyRequest request, HttpServletResponse response) {

        Map<String, Object> param = BillService.queryHrMoneyRecord(request);
        ExcelUtil.download(response, (List<PayRecord>)param.get("list"), ExcelUtil.payRecord, "支付记录", param.get("name").toString() + "收款记录");
    }
    /**
     * 小时工账目统计/每月
     * @param
     */
    @GetMapping("worker/account/statistics/{id}")
    public ResultDO accountStatistics(@PathVariable String id) {
        BillService.queryWorkerStatistics(id);
        return null;
    }
}
