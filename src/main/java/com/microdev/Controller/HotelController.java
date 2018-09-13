package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.model.Company;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import com.microdev.service.TaskHrCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用人单位信息相关的Api
 */
@RestController
public class HotelController {

    @Autowired
    CompanyService companyService;

    @Autowired
    TaskHrCompanyService taskHrCompanyService;
    /**
     * 根据ID查找用人单位
     */
    @GetMapping("/hotels/{id}")
    public ResultDO getById(@PathVariable String id) {
        return companyService.getCompanyById(id);
    }
    /**
     *查找用人单位下的人力资源公司
     */
    @PostMapping("/hotels/hrcompanies")
    public ResultDO gethotelHrCompanies(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        return companyService.hotelHrCompanies(paging.getPaginator(),paging.getSelector());
    }
    /**
     *查找用人单位下的小时工
     */
    @PostMapping("/hotels/workers")
    public ResultDO gethotelWorkers(@RequestBody PagingDO<HrQueryWorkerDTO> paging) {
        return companyService.hotelWorkers(paging.getPaginator(),paging.getSelector());
    }
    /**
     *查找用人单位可添加的人力资源公司
     *//*
    @GetMapping("/hotels/{id}/nothrcompanies")
    public ResultDO gethotelNoHrCompanies(@PathVariable String id) {
        return companyService.hotelNotHrCompanies(id);
    }*/
    /**
     * 添加公司
     */
    @PostMapping("/Companys")
    public ResultDO createCompany(@RequestBody Company companyDTO) {
        return companyService.createCompany(companyDTO);
    }
    /**
     * 用人单位处理人力资源公司绑定
     */
    @GetMapping("/hotels/{messageId}/add/{status}")
    public ResultDO addHrCompany(@PathVariable String messageId,@PathVariable String status) {
        return companyService.hotelAddHrCompanyById(messageId, status, 1);
    }
    /**
     * 分页查询用人单位信息
     */
    @PostMapping("/hotels/search")
    public ResultDO getPageData(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        CompanyQueryDTO queryDTO= paging.getSelector();
        queryDTO.setCompanyType(1);
        paging.setSelector(queryDTO);
        return companyService.pagingCompanys(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 修改用人单位基本信息
     */
    @PutMapping("/hotels")
    public ResultDO updateCompany(@RequestBody Company companyDTO) {
        companyDTO.setCompanyType(1);
        return companyService.updateCompany(companyDTO);
    }
    /**
     * 审核用人单位
     */
    @GetMapping("/hotels/{id}/status/{code}")
    public ResultDO getById(@PathVariable String id,@PathVariable Integer code) {
        return companyService.confirmCompany(id,code);
    }
    /**
     * 用人单位移除人力资源公司
     */
    @DeleteMapping("/hotels/{hotelId}/remove/{hrCompanyId}")
    public ResultDO removeHrCompany(@PathVariable String hotelId,@PathVariable String hrCompanyId) {
        HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,1,1);
        return companyService.hotelRemoveHrCompany(hotelHr);
    }
    /**
     * 用人单位反馈补签
     */
    @PostMapping("/hotels/supplementResponse")
    public ResultDO supplementResponse(@RequestBody Map<String, String> param) {
        return ResultDO.buildSuccess(companyService.supplementResponse(param.get("messageId"), param.get("status")));
    }
    /**
     * 用人单位申请替换小时工(任务已开始)
     */
    @PostMapping("/hotels/changeWorker")
    public ResultDO changeWorker(@RequestBody Map<String, Object> map) {
        return ResultDO.buildSuccess(companyService.changeWorker(map));
    }
    /**
     * 用人单位主动替换小时工(任务已开始)
     */
    @PostMapping("/hotels/initiative/changeWorker")
    public ResultDO changeOwnWorker(@RequestBody ChangeWorkerParam param) {
        return companyService.changeOwnWorker(param);
    }
    /**
     * 用人单位账目明细
     */
    @PostMapping("/hotels/accountDetails")
    public ResultDO accountDetails(String hotelId, Integer page, Integer pageSize) {
        return ResultDO.buildSuccess(companyService.accountDetail(hotelId, page, pageSize));
    }
    /**
     * 用人单位待处理事务分页信息
     */
    @PostMapping("/hotels/hotelWaitTaskPage")
    public ResultDO hotelWaitTaskPage(@RequestBody MessageRequest request) {
        return ResultDO.buildSuccess(companyService.hotelWaitTaskDetails(request));
    }
    /**
     * 用人单位待处理事务详情
     */
    @Deprecated
    @PostMapping("/hotels/hotelWaitTaskDetails")
    public ResultDO hotelWaitTaskDetails(@RequestBody PendRequest request) {
        return ResultDO.buildSuccess(companyService.showWaitInfo(request));
    }
    /**
     * 用人单位再发布
     */
    @PostMapping("/hotels/hotelPublishAgain")
    public ResultDO hotelPublishAgain(@RequestBody HotelDeployInfoRequest request) {
        return ResultDO.buildSuccess(companyService.hotelPublish(request));
    }
    /**
     * 用人单位处理小时工加时
     */
    @PostMapping("/hotels/handleExpandWork")
    public ResultDO handleExpandWork(@RequestBody Map<String, String> param) {
        return ResultDO.buildSuccess(companyService.workExpand(param.get("messageId"), param.get("status")));
    }
    /**
     * 用人单位申请绑定人力资源公司
     */
    @PostMapping("/hotels/apply/bindHrCompany")
    public ResultDO bindHrCompany(@RequestBody HotelHrIdBindDTO dto) {
        return companyService.hotelAddHrCompanySet(dto);
    }
    /**
     * 用人单位申请绑定小时工
     */
    @PostMapping("/hotels/apply/bindWorker")
    public ResultDO bindWorker(@RequestBody HotelHrIdBindDTO dto) {
        return companyService.hotelAddWorkerSet(dto);
    }
    /**
     * 用人单位申请解绑人力资源公司
     */
    @PostMapping("/hotels/relieve/bindHrCompany")
    public ResultDO relieveHrCompany(@RequestBody HotelHrIdBindDTO dto) {
        return companyService.hotelRelieveHrCompanySet(dto);
    }

    /**
     * 查询合作的人力公司信息
     * @param paging
     * @return
     */
    @PostMapping("/hotels/cooperate/hr")
    public ResultDO hotelsCooperateHr(@RequestBody PagingDO<QueryCooperateRequest> paging) {
        Paginator paginator = paging.getPaginator();
        return companyService.selectCooperatorHr(paging.getSelector(), paginator.getPage(), paginator.getPageSize());
    }

    /**
     * 用人单位查询待审核的人力公司
     * @param paging
     * @return
     */
    @PostMapping("/hotels/examine/companies")
    public ResultDO hotelsExamineCompanies(@RequestBody PagingDO<QueryCooperateRequest> paging) {
        return companyService.hotelExamineHr(paging.getSelector(), paging.getPaginator());
    }

    /**
     * 用人单位处理小时工请假
     * @param param
     * @return
     */
    @PostMapping("/hotels/handle/workerLeave")
    public ResultDO hotelsHandleWorkerLeave(@RequestBody Map<String, String> param) {

        return companyService.hotelHandleLeave(param.get("messageId"), param.get("status"));
    }
    /**
     * 用人单位处理人力公司申请调配
     * @return
     */
    @PostMapping("/apply/for/deployment/handle")
    public ResultDO applyfordeploymenthandle(@RequestBody CreateTaskRequest request) {

        return companyService.deploymentHandle(request);
    }

    /**
     * 用人单位处理小时工工作记录
     * @param record
     * @return
     */
    @PostMapping("/hotels/handle/workerRecord")
    public ResultDO hotelsHandleRecords(@RequestBody HotelHandleWorkerRecord record) {

        return companyService.hotelHandleWorkerRecord(record);
    }

    /**
     * 下载用人单位绑定的人力
     * @param response
     * @param request
     */
    @GetMapping("/hotel/hr/download")
    public void hotelBindHrDownload(HttpServletResponse response,@ModelAttribute CompanyQueryDTO request) {

        List<CompanyCooperate> list = companyService.queryHotelBindHr(request);
        String name = "";
        Company c = companyService.selectById(request.getId());
        if (c != null) {
            name = c.getName();
        }
        ExcelUtil.download(response, list, ExcelUtil.cooperate, "合作的人力公司", name + "合作的人力");
    }

    /**
     * 下载所有用人单位
     * @param response
     */
    @GetMapping("/hotel/info/download")
    public void downloadHotelInfo(HttpServletResponse response) {
        List<EmployerInfo> list = companyService.queryHotelInfo();
        ExcelUtil.download(response, list, ExcelUtil.hotelInfo, "用人单位", "用人单位");
    }
   /**
     * 用人单位同意小时工取消任务并派发新任务
     * @return
     */
    @PostMapping("/hotel/cancel/handle")
    public ResultDO hrAgreeWorkerCancelTask(@RequestBody HotelCancelParam request) {
        return companyService.hotelCancelHandle(request);
    }
    /**
     * 酒店处理小时工拒绝任务并派发任务
     * @param
     * @param
     * @return
     */
    @PostMapping("/hotel/agree/distribute")
    public ResultDO hotelcompaniesAgreePost(@RequestBody HotelCancelParam request) {
        return companyService.hotelAgreeWorkerRefuseAndPost(request.getMessageId(), request.getWorkerId ());
    }
}
