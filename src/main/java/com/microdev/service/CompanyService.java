package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.User;
import com.microdev.param.*;

import java.util.Map;
import java.util.Set;


public interface CompanyService extends IService<Company> {
    /**
     * 分页查询
     */
    ResultDO pagingCompanys(Paginator paginator, CompanyQueryDTO queryDTO);
    /**
     * 获取已添加该人力公司的所有酒店
     */
    ResultDO hrCompanyHotels(Paginator paginator, CompanyQueryDTO queryDTO);

    /**
     * 根据Id 获取公司信息
     */
    ResultDO getCompanyById(String id);
    /**
     * 酒店绑定添加合作的人力资源公司
     */
    ResultDO hotelAddHrCompanyById(String hotelId, String hrCompanyId, Integer type);
    /**
     * 添加人力资源公司
     */
    ResultDO createCompany(Company companyDTO);
    /**
     * 修改人力资源公司
     */
    ResultDO updateCompany(Company companyDTO);
    /**
     * 审核人力资源公司
     */
    ResultDO confirmCompany(String id,Integer code);
    /**
     * 酒店移除合作的人力资源公司
     */
    ResultDO hotelRemoveHrCompany(HotelHrIdBindDTO hotelHrDTO);
    /**
     * 获取酒店下的人力公司
     */
    ResultDO hotelHrCompanies(Paginator paginator, CompanyQueryDTO request);
    /**
     * 获取酒店可以添加的人力公司
     */
    ResultDO hotelNotHrCompanies(String id);
    /**
     * 获取人力公司可以添加的酒店
     */
    ResultDO hrCompanyNotHotels(String id);
    /**
     * 酒店反馈补签
     */
    String supplementResponse(String id, String status);
    /**
     * 酒店申请替换小时工
     */
    boolean changeWorker(Map<String, Object> map);
    /**
     * 酒店账目明细
     */
    Map<String, Object> accountDetail(String hotelId, Integer page, Integer pageSize);
    /**
     * 分页查询酒店事务
     * @param
     * @return
     */
    PageInfo<Map<String, Object>> hotelWaitTaskDetails(MessageRequest request);
    /**
     * 展示酒店待处理的信息详情
     * @param request
     * @return
     */
    ResultDO showWaitInfo(PendRequest request);
    /**
     * 酒店再发布
     * @param request
     * @return
     */
    String hotelPublish(HotelDeployInfoRequest request);
    /**
     * 酒店处理小时工加时
     * @param id
     * @param status
     * @return
     */
    String workExpand(String id, String status);
    /**
     * 酒店添加人力资源公司
     * @param dto
     * @return
     */
    ResultDO hotelAddHrCompanySet(HotelHrIdBindDTO dto);

    /**
     * 人力公司同意解绑小时工
     * @param messageId
     * @return
     */
    String hrUnbindWorker(String messageId, String status);

    /**
     * 人力查询待审核的酒店信息
     * @param hrCompanyId
     * @param page
     * @param pageNum
     * @return
     */
    ResultDO selectExamineCompanies(String hrCompanyId, Integer page, Integer pageNum);

    /**
     * 查询合作的人力公司信息
     * @param map
     * @param page
     * @param pageNum
     * @return
     */
    ResultDO selectCooperatorHr(QueryCooperateRequest map, Integer page, Integer pageNum);

    /**
     * 人力处理酒店绑定申请
     * @param messageId     消息id
     * @param status        0拒绝1同意
     * @return
     */
    ResultDO hrHandlerHotelBind(String messageId, String status);

    /**
     * 人力查询合作的酒店
     * @param map
     * @return
     */
    ResultDO hrQueryCooperatorHotel(QueryCooperateRequest map, Paginator paginator);

    /**
     * 酒店查询待审核的人力公司
     * @param request
     * @param paginator
     * @return
     */
    ResultDO hotelExamineHr(QueryCooperateRequest request, Paginator paginator);

    /**
     * 酒店处理小时工请假
     * @param messageId    消息id
     * @param status       0拒绝1同意
     * @return
     */
    ResultDO hotelHandleLeave(String messageId, String status);

    ResultDO deploymentHandle(CreateTaskRequest createTaskRequest);

    /**
     * 酒店处理小时工工作记录
     * @param record
     * @return
     */
    ResultDO hotelHandleWorkerRecord(HotelHandleWorkerRecord record);
}
