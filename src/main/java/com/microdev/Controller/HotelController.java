package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 酒店信息相关的Api
 */
@RestController
public class HotelController {

    private static final Logger logger = LoggerFactory.getLogger(HotelController.class);

    @Autowired
    CompanyService companyService;
    /**
     * 根据ID查找酒店
     */
    @GetMapping("/hotels/{id}")
    public ResultDO getById(@PathVariable String id) {
        return companyService.getCompanyById(id);
    }
    /**
     *查找酒店下的人力资源公司
     */
    @PostMapping("/hotels/hrcompanies")
    public ResultDO gethotelHrCompanies(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        logger.info("gethotelHrCompanies param:" + paging.toString());
        return companyService.hotelHrCompanies(paging.getPaginator(),paging.getSelector());
    }
    /**
     *查找酒店可添加的人力资源公司
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
     * 酒店处理人力资源公司绑定
     */
    @GetMapping("/hotels/{messageId}/add/{status}")
    public ResultDO addHrCompany(@PathVariable String messageId,@PathVariable String status) {

        return companyService.hotelAddHrCompanyById(messageId, status, 1);
    }
    /**
     * 分页查询酒店信息
     */
    @PostMapping("/hotels/search")
    public ResultDO getPageData(@RequestBody PagingDO<CompanyQueryDTO> paging) {
        CompanyQueryDTO queryDTO= paging.getSelector();
        queryDTO.setCompanyType(1);
        paging.setSelector(queryDTO);
        System.out.println ("param:"+paging);
        return companyService.pagingCompanys(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 修改酒店基本信息
     */
    @PutMapping("/hotels")
    public ResultDO updateCompany(@RequestBody Company companyDTO) {
        companyDTO.setCompanyType(1);
        return companyService.updateCompany(companyDTO);
    }
    /**
     * 审核酒店
     */
    @PutMapping("/hotels/{id}/status/{code}")
    public ResultDO getById(@PathVariable String id,@PathVariable Integer code) {
        return companyService.confirmCompany(id,code);
    }
    /**
     * 酒店移除人力资源公司
     */
    @DeleteMapping("/hotels/{hotelId}/remove/{hrCompanyId}")
    public ResultDO removeHrCompany(@PathVariable String hotelId,@PathVariable String hrCompanyId) {
        HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,1,1);
        return companyService.hotelRemoveHrCompany(hotelHr);
    }
    /**
     * 酒店反馈补签
     */
    @PostMapping("/hotels/supplementResponse")
    public ResultDO supplementResponse(Map<String, String> param) {
        return ResultDO.buildSuccess(companyService.supplementResponse(param.get("messageId"), param.get("status")));
    }
    /**
     * 酒店申请替换小时工
     */
    @PostMapping("/hotels/changeWorker")
    public ResultDO changeWorker(Map<String, Object> map) {
        return ResultDO.buildSuccess(companyService.changeWorker(map));
    }
    /**
     * 酒店账目明细
     */
    @PostMapping("/hotels/accountDetails")
    public ResultDO accountDetails(String hotelId, Integer page, Integer pageSize) {
        return ResultDO.buildSuccess(companyService.accountDetail(hotelId, page, pageSize));
    }
    /**
     * 酒店待处理事务分页信息
     */
    @PostMapping("/hotels/hotelWaitTaskPage")
    public ResultDO hotelWaitTaskPage(MessageRequest request) {
        return ResultDO.buildSuccess(companyService.hotelWaitTaskDetails(request));
    }
    /**
     * 酒店待处理事务详情
     */
    @PostMapping("/hotels/hotelWaitTaskDetails")
    public ResultDO hotelWaitTaskDetails(@RequestBody PendRequest request) {
        return ResultDO.buildSuccess(companyService.showWaitInfo(request));
    }
    /**
     * 酒店再发布
     */
    @PostMapping("/hotels/hotelPublishAgain")
    public ResultDO hotelPublishAgain(@RequestBody HotelDeployInfoRequest request) {
        return ResultDO.buildSuccess(companyService.hotelPublish(request));
    }
    /**
     * 酒店处理小时工加时
     */
    @PostMapping("/hotels/handleExpandWork")
    public ResultDO handleExpandWork(Map<String, String> param) {
        return ResultDO.buildSuccess(companyService.workExpand(param.get("messageId"), param.get("status")));
    }
    /**
     * 酒店申请绑定人力资源公司
     */
    @PostMapping("/hotels/apply/bindHrCompany")
    public ResultDO bindHrCompany(@RequestBody HotelHrIdBindDTO dto) {
        return companyService.hotelAddHrCompanySet(dto);
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
     * 酒店查询待审核的人力公司
     * @param paging
     * @return
     */
    @PostMapping("/hotels/examine/companies")
    public ResultDO hotelsExamineCompanies(@RequestBody PagingDO<QueryCooperateRequest> paging) {

        return companyService.hotelExamineHr(paging.getSelector(), paging.getPaginator());
    }

    /**
     * 酒店处理小时工请假
     * @param param
     * @return
     */
    @PostMapping("/hotels/handle/workerLeave")
    public ResultDO hotelsHandleWorkerLeave(Map<String, String> param) {

        return companyService.hotelHandleLeave(param.get("messageId"), param.get("status"));
    }
    /**
     * 酒店处理人力公司申请调配
     * @param param status 0同意 1拒绝
     * @return
     */
    @PostMapping("/apply/for/deployment/handle")
    public ResultDO applyfordeploymenthandle(Map<String, String> param) {

        return companyService.deploymentHandle(param.get("messageId"), param.get("status"),param.get("reason"));
    }

}
