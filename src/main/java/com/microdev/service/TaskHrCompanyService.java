package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.HrPayWorkerRequest;
import com.microdev.param.HrTaskDistributeRequest;
import com.microdev.param.TaskHrQueryDTO;

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
    void TaskHraccept(String id, String messageId);
    /**
     * 人力公司拒绝任务
     */
    void TaskHrrefuse(String id, String messageId);
    /**
     * 人力公司申请调配
     */
    void TaskHrallocate(String id, String reason, Integer number);
    /**
     * 酒店查询账目
     */
    ResultDO getHotelBill(String hotelId);
    /**
     * 人力公司按酒店查询账目
     */
    ResultDO getCompanyBillHotel(String hrCompanyId);
    /**
     * 人力公司按小时工查询账目
     */
    ResultDO getCompanyBillWorker(String hrCompanyId);
    /**
     * 小时工按人力公司工查询账目
     *
     */
    ResultDO getWorkerBill(String workerId);
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

}
