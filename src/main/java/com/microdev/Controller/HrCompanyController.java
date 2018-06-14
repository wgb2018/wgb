package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import com.microdev.service.TaskHrCompanyService;
import com.microdev.service.UserCompanyService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人力公司信息相关的Api
 */
@RestController
public class HrCompanyController {

    private static final Logger logger = LoggerFactory.getLogger(HrCompanyController.class);
    @Autowired
    CompanyService companyService;
    @Autowired
    private UserCompanyService userCompanyService;
    @Autowired
    private TaskHrCompanyService taskHrCompanyService;
    /**
     * 分页查询人力资源公司
     */
    @PostMapping("/hrcompanies/search")
    public ResultDO getPageData(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        CompanyQueryDTO queryDTO= paging.getSelector();
        queryDTO.setCompanyType(2);
        paging.setSelector(queryDTO);
        return companyService.pagingCompanys(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 获取已添加该人力公司的所有酒店
     */
    @PostMapping("/hrcompanies/hotels")
    public ResultDO getHrCompaniesHotels(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        logger.error("getHrCompaniesHotels:" + paging.toString());
        return companyService.hrCompanyHotels(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 获取人力公司可以添加的酒店
     *//*
    @GetMapping("/hrcompanies/{id}/hotels")
    public ResultDO getHrCompaniesNotHotels(@PathVariable String id) {
        return companyService.hrCompanyNotHotels(id);
    }*/
    /**
     * 根据ID查找查找人力资源公司信息
     */
    @GetMapping("/hrcompanies/{id}")
    public ResultDO getById(@PathVariable String id) {
        return companyService.getCompanyById(id);
    }
    /**
     * 添加人力资源公司
     */
    @PostMapping("/hrcompanies")
    public ResultDO createCompany(@RequestBody Company companyDTO) {
        companyDTO.setCompanyType(2);
        return companyService.createCompany(companyDTO);
    }
    /**
     * 人力资源处理合作酒店
     */
    @GetMapping("/hrcompanies/{messageId}/add/{status}")
    public ResultDO addHrCompany(@PathVariable String messageId,@PathVariable String status) {
        //HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,2);
        return companyService.hotelAddHrCompanyById(messageId, status, 2);
    }
    /**
     * 修改人力资源公司
     */
    @PutMapping("/hrcompanies")
    public ResultDO updateCompany(@RequestBody Company companyDTO) {
        companyDTO.setCompanyType(2);
        return companyService.updateCompany(companyDTO);
    }
    /**
     * 审核人力资源公司
     */
    @PutMapping("/hrcompanies/{id}/status/{code}")
    public ResultDO getById(@PathVariable String id,@PathVariable Integer code) {
        return companyService.confirmCompany(id,code);
    }
    /**
     * 人力资源公司移除合作酒店
     */
    @DeleteMapping("/hrcompanies/{hrCompanyId}/remove/{hotelId}")
    public ResultDO removeHrCompany(@PathVariable String hrCompanyId,@PathVariable String hotelId) {
        HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,2,2);
        return companyService.hotelRemoveHrCompany(hotelHr);
    }
    /**
     * 人力资源公司申请绑定合作酒店
     */
    @PostMapping("/hrcompanies/apply/bindCompany")
    public ResultDO bindCompany(@RequestBody HotelHrIdBindDTO dto) {

        return companyService.hotelAddHrCompanySet(dto);
    }

    /**
     * 人力申请绑定小时工
     */
    @PostMapping("/hrcompanies/apply/bindWorkers")
    public ResultDO bindWorkers(@RequestBody HotelHrIdBindDTO dto) {

        return ResultDO.buildSuccess(userCompanyService.hrApplyBindWorker(dto.getHrId(), dto.getSet()));
    }

    /**
     * 人力公司反馈小时工解绑申请
     * @param messageId     消息id
     * @param status        1同意
     * @return
     */
    @GetMapping("/hrcompanies/{messageId}/unbind/{status}")
    public ResultDO hrCompanyUnbindWorker(@PathVariable String messageId,@PathVariable String status) {

        return ResultDO.buildSuccess(companyService.hrUnbindWorker(messageId, status));
    }

    /**
     * 人力查询待审核的酒店信息
     * @param page
     * @return
     */
    @PostMapping("/hrcompanies/examine/companies")
    public ResultDO hrCompaniesExamineCompanies(@RequestBody PagingDO<QueryCooperateRequest> page) {
        Paginator paginator = page.getPaginator();
        return companyService.selectExamineCompanies(page.getSelector().getId(), paginator.getPage(), paginator.getPageSize());
    }

    /**
     * 人力查询合作的小时工
     * @return
     */
    @PostMapping("/hrcompanies/manager/workers")
    public ResultDO hrcompaniesManagerWorkers(@RequestBody PagingDO<QueryCooperateRequest> page) {
        Paginator paginator = page.getPaginator();
        return userCompanyService.selectWorkerCooperate(page.getSelector(), paginator.getPage(), paginator.getPageSize());
    }

    /**
     * 人力处理酒店绑定申请
     * @param messageId
     * @param status    0拒绝1同意
     * @return
     */
    @GetMapping("/hrcompanies/{messageId}/handle/{status}")
    public ResultDO hrcompanyHandleBind(@PathVariable String messageId,@PathVariable String status) {

        return companyService.hrHandlerHotelBind(messageId, status);
    }

    /**
     * 人力查询合作的酒店
     * @param page
     * @return
     */
    @PostMapping("/hrcompanies/cooperate/hotels")
    public ResultDO hrcompaniesCooperateInfo(@RequestBody PagingDO<QueryCooperateRequest> page) {

        return companyService.hrQueryCooperatorHotel(page.getSelector(), page.getPaginator());
    }

    /**
     * 人力再派单
     * @return
     */
    @PostMapping("/hrcompanies/assignment/workers")
    public ResultDO hrcompaniesAssignmentWorkers(@RequestBody AssignmentRequest request) {
        return taskHrCompanyService.hrAssignmentTask(request);
    }

    /**
     * 人力主动换小时工
     * @return
     */
    @GetMapping("/{workerId}/exchange/worker/{taskWorkerId}")
    public ResultDO hrcompanyExchangeWorker(@PathVariable String taskWorkerId,@PathVariable String workerId) {
        return taskHrCompanyService.exchangeWorker(taskWorkerId,workerId);
    }

    /**
     * 人力拒绝酒店的申请调换
     * @param messageId
     * @return
     */
    @GetMapping("/hrcompanies/refuse/hotel/{messageId}")
    public ResultDO refuseWorkersRefuseTask(@PathVariable String messageId) {

        return taskHrCompanyService.hrRefuseHotelSwapWorker(messageId);
    }

    /**
     * 人力拒绝小时工取消任务
     * @param messageId
     * @return
     */
    @GetMapping("/hrcompanies/refuse/{messageId}/worker/cancel")
    public ResultDO hrHandleWorkerCancelTask(@PathVariable String messageId) {

        return taskHrCompanyService.hrHandleWorkerTaskCancel(messageId);
    }

    /**
     * 人力同意小时工取消任务并派发新任务
     * @param messageId
     * @param workerId
     * @return
     */
    @GetMapping("/hrcompanies/agree/{messageId}/cancel/{workerId}")
    public ResultDO hrAgreeWorkerCancelTask(@PathVariable String messageId,@PathVariable String workerId) {

        return taskHrCompanyService.hrAgrreWorkerTaskCancel(messageId, workerId);
    }

    /**
     * 人力处理酒店付款.
     * @param messageId
     * @param status        0拒绝1同意
     * @return
     */
    @GetMapping("/hrcompanies/{messageId}/account/{status}")
    public ResultDO hrcompaniesHandleAccount(@PathVariable String messageId,@PathVariable String status) {
        return taskHrCompanyService.hrHandleIncome(messageId, status);
    }

    /**
     * 人力同意小时工拒绝任务并派发任务
     * @param messageId
     * @param workerId
     * @return
     */
    @GetMapping("/hrcompanies/agree/{messageId}/distribute/{workerId}")
    public ResultDO hrcompaniesAgreePost(@PathVariable String messageId,@PathVariable String workerId) {
        return taskHrCompanyService.hrAgreeWorkerRefuseAndPost(messageId, workerId);
    }
}
