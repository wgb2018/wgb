package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.User;
import com.microdev.param.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface CompanyService extends IService<Company> {
    /**
     * 分页查询
     */
    ResultDO pagingCompanys(Paginator paginator, CompanyQueryDTO queryDTO);
    /**
     * 获取已添加该人力公司的所有用人单位
     */
    ResultDO hrCompanyHotels(Paginator paginator, CompanyQueryDTO queryDTO);

    /**
     * 根据Id 获取公司信息
     */
    ResultDO getCompanyById(String id);
    /**
     * 用人单位绑定添加合作的人力资源公司
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
     * 用人单位移除合作的人力资源公司
     */
    ResultDO hotelRemoveHrCompany(HotelHrIdBindDTO hotelHrDTO);
    /**
     * 获取用人单位下的人力公司
     */
    ResultDO hotelHrCompanies(Paginator paginator, CompanyQueryDTO request);
    /**
     * 获取用人单位下的小时工
     */
    ResultDO hotelWorkers(Paginator paginator, HrQueryWorkerDTO dto);
    /**
     * 获取用人单位可以添加的人力公司
     */
    ResultDO hotelNotHrCompanies(String id);
    /**
     * 获取人力公司可以添加的用人单位
     */
    ResultDO hrCompanyNotHotels(String id);
    /**
     * 用人单位反馈补签
     */
    String supplementResponse(String id, String status);
    /**
     * 用人单位申请替换小时工
     */
    String changeWorker(Map<String, Object> map);
    /**
     * 用人单位主动替换小时工
     */
    ResultDO changeOwnWorker(ChangeWorkerParam map);

    /**
     * 用人单位账目明细
     */
    Map<String, Object> accountDetail(String hotelId, Integer page, Integer pageSize);
    /**
     * 分页查询用人单位事务
     * @param
     * @return
     */
    PageInfo<Map<String, Object>> hotelWaitTaskDetails(MessageRequest request);
    /**
     * 展示用人单位待处理的信息详情
     * @param request
     * @return
     */
    ResultDO showWaitInfo(PendRequest request);
    /**
     * 用人单位再发布
     * @param request
     * @return
     */
    String hotelPublish(HotelDeployInfoRequest request);
    /**
     * 用人单位处理小时工加时
     * @param id
     * @param status
     * @return
     */
    String workExpand(String id, String status);
    /**
     * 用人单位添加人力资源公司
     * @param dto
     * @return
     */
    ResultDO hotelAddHrCompanySet(HotelHrIdBindDTO dto);
    /**
     * 用人单位添加小时工
     * @param dto
     * @return
     */
    ResultDO hotelAddWorkerSet(HotelHrIdBindDTO dto);

    /**
     * 人力公司同意解绑小时工
     * @param messageId
     * @return
     */
    String hrUnbindWorker(String messageId, String status);

    /**
     * 人力查询待审核的用人单位信息
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
     * 人力处理用人单位绑定申请
     * @param messageId     消息id
     * @param status        0拒绝1同意
     * @return
     */
    ResultDO hrHandlerHotelBind(String messageId, String status);

    /**
     * 人力查询合作的用人单位
     * @param map
     * @return
     */
    ResultDO hrQueryCooperatorHotel(QueryCooperateRequest map, Paginator paginator);

    /**
     * 用人单位查询待审核的人力公司
     * @param request
     * @param paginator
     * @return
     */
    ResultDO hotelExamineHr(QueryCooperateRequest request, Paginator paginator);

    /**
     * 用人单位处理小时工请假
     * @param messageId    消息id
     * @param status       0拒绝1同意
     * @return
     */
    ResultDO hotelHandleLeave(String messageId, String status);

    ResultDO deploymentHandle(CreateTaskRequest createTaskRequest);

    /**
     * 用人单位处理小时工工作记录
     * @param record
     * @return
     */
    ResultDO hotelHandleWorkerRecord(HotelHandleWorkerRecord record);
    /**
     * 用人单位申请解绑人力公司
     */
    ResultDO hotelRelieveHrCompanySet(HotelHrIdBindDTO dto);

    /**
     * 查询人力绑定的用人单位
     * @param request
     * @return
     */
    List<CompanyCooperate> queryHrBindHotel(CompanyQueryDTO request);

    /**
     * 查询用人单位绑定的人力
     * @param request
     * @return
     */
    List<CompanyCooperate> queryHotelBindHr(CompanyQueryDTO request);

    /**
     * 查询所有用人单位信息
     * @return
     */
    List<EmployerInfo> queryHotelInfo();

    /**
     * 查询所有人力信息
     * @return
     */
    List<HrInfo> queryInfo();
    /**
     * 处理小时工申请取消任务
     * @return
     */
    ResultDO hotelCancelHandle(HotelCancelParam request);
    /**
     * 用人单位小时工拒绝任务并派发任务
     * @param messageId
     * @param workerId
     * @return
     */
    ResultDO hotelAgreeWorkerRefuseAndPost(String messageId, String workerId);
}
