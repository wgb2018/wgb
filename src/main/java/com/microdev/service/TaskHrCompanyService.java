package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.*;

import java.util.Map;

public interface TaskHrCompanyService extends IService<TaskHrCompany> {
    /**
     * 查看人力资源公司的任务
     */
    ResultDO getTaskHrCompanyById(String id);
    /**
     * 人力资源公司任务派发
     */
    ResultDO TaskHrDistribute(HrTaskDistributeRequest hrTaskDis);
    /**
     * 分页获取任务数据
     */
    ResultDO getPageTasks(Paginator paginator, TaskHrQueryDTO taskHrQueryDTO);
    /**
     * 人力公司支付小时工
     */
    ResultDO hrPayWorkers(HrPayWorkerRequest payWorkerRequest);
    /**
     * 人力公司接受任务
     */
    void TaskHraccept(String messageId);
    /**
     * 人力公司拒绝任务
     */
    void TaskHrrefuse(String messageId, String reason);
    /**
     * 人力公司申请调配
     */
    String TaskHrallocate(String id, String reason, Integer number);
    /**
     * 酒店查询账目
     */
    ResultDO getHotelBill(Paginator paginator, BillRequest request);
    /**
     * 人力公司按酒店查询账目
     */
    ResultDO getCompanyBillHotel(Paginator paginator, BillRequest request);
    /**
     * 人力公司按小时工查询账目
     */
    ResultDO getCompanyBillWorker(Paginator paginator,BillRequest request);
    /**
     * 小时工按人力公司工查询账目
     *
     */
    ResultDO getWorkerBill(Paginator paginator,BillRequest request);
    /**
     * 人力公司申请调配.
     * @param map
     * @return
     */
    ResultDO hrApplyChangeWorker(Map<String, Object> map);

    /**
     * 统计人力公司待处理未读数据
     * @param hrCompanyId
     * @return
     */
    int selectUnreadCount(String hrCompanyId);

    /**
     * 统计人力已完成未读数据
     * @param hrCompanyId
     * @return
     */
    int selectCompleteCount(String hrCompanyId);

    /**
     * 更新人力任务查看标识
     * @param taskHrCompanyId
     * @param status            1未完成已读 3已完成已读
     * @return
     */
    String updateTaskHrStatus(String taskHrCompanyId, Integer status);

    /**
     * PC端人力接受任务
     * @param id
     * @return
     */
    String taskHracceptPC(String id);

    /**
     * PC端人力拒绝任务
     * @param id
     * @return
     */
    String TaskHrrefusePC(String id,String reason);

    /**
     * 人力再派发任务
     * @param request
     * @return
     */
    ResultDO hrAssignmentTask(AssignmentRequest request);

    /**
     * 人力拒绝酒店调换小时工的申请
     * @param messageId
     * @return
     */
    ResultDO hrRefuseHotelSwapWorker(String messageId);

    /**
     * 人力处理小时工取消任务
     * @param messageId
     * @return
     */
    ResultDO hrHandleWorkerTaskCancel(String messageId);

    /**
     * 人力同意小时工取消任务并派发新任务。
     * @param messageId
     * @param workerId
     * @return
     */
    ResultDO hrAgrreWorkerTaskCancel(String messageId, String workerId);

    /**
     * 人力公司处理酒店
     * @param messageId
     * @param status        0拒绝1同意
     * @param reason
     * @return
     */
    ResultDO hrHandleIncome(String messageId, String status, String reason);

    /**
     * 人力同意小时工拒绝任务并派发任务
     * @param messageParamDTO
     * @return
     */
    ResultDO hrAgreeWorkerRefuseAndPost(MessageParamDTO messageParamDTO);
}
