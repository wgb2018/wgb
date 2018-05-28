package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.model.Company;
import com.microdev.param.CompanyQueryDTO;
import com.microdev.param.HotelHrIdBindDTO;
import com.microdev.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 人力公司信息相关的Api
 */
@RestController
public class HrCompanyController {

    @Autowired
    CompanyService companyService;
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
    public ResultDO addHrCompany(@PathVariable String hrCompanyId,@PathVariable String hotelId) {
        HotelHrIdBindDTO hotelHr=new HotelHrIdBindDTO(hotelId,hrCompanyId,2);
        return companyService.hotelAddHrCompanyById(hotelHr);
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
}
