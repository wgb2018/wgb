package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.User;
import com.microdev.param.CompanyQueryDTO;
import com.microdev.param.HotelHrIdBindDTO;
import com.microdev.service.CompanyService;
import com.microdev.service.UserCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 人力资源公司添加合作酒店
     */
    @PostMapping("/hrcompanies/{hrCompanyId}/add/{hotelId}")
    public ResultDO addHrCompany(@PathVariable String hrCompanyId,@PathVariable String hotelId, String messageId) {
        //HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,2);
        return companyService.hotelAddHrCompanyById(hotelId, hrCompanyId, messageId, 2);
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
        logger.info("bindCompany param:" + dto.toString());
        return companyService.hotelAddHrCompanySet(dto);
    }

    /**
     * 人力申请绑定小时工
     */
    @PostMapping("/hrcompanies/apply/bindWorkers")
    public ResultDO bindWorkers(@RequestBody Map<String, Object> param) {

        return ResultDO.buildSuccess(userCompanyService.hrApplyBindWorker((String)param.get("hrId"), (List<String>) param.get("set")));
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
    public ResultDO hrCompaniesExamineCompanies(@RequestBody PagingDO<String> page) {
        Paginator paginator = page.getPaginator();
        return companyService.selectExamineCompanies(page.getSelector(), paginator.getPage(), paginator.getPageSize());
    }

    /**
     * 人力查询合作的小时工
     * @return
     */
    @PostMapping("/hrcompanies/manager/workers")
    public ResultDO hrcompaniesManagerWorkers(@RequestBody PagingDO<String> page) {
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

        return ResultDO.buildSuccess(companyService.hrHandlerHotelBind(messageId, status));
    }

    /**
     * 人力查询合作的酒店
     * @param page
     * @return
     */
    @PostMapping("/hrcompanies/cooperate/hotels")
    public ResultDO hrcompaniesCooperateInfo(@RequestBody PagingDO<String> page) {

        return companyService.hrQueryCooperatorHotel(page.getSelector(), page.getPaginator());
    }
}
