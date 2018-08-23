package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Task;
import com.microdev.param.*;

import java.util.List;

public interface TaskService extends IService<Task> {
    /**
     * 创建任务
     */
    ResultDO createTask(CreateTaskRequest taskDTO);
    /**
     * 查询单个任务详情
     */
    ResultDO getTaskById(String id);
    /**
     * 分页获取任务数据
     */
    ResultDO getPageTasks(Paginator paginator, TaskQueryDTO taskQueryDTO);
    /**
     * 用人单位支付人力公司
     */
    ResultDO hotelPayHr(PayParam PayHrParam);

    ResultDO hotelPayWoreker(PayParam payParam);

    /**
     * 用人单位再次派发任务
     * @param request
     * @return
     */
    ResultDO hotelAgainSendTask(CreateTaskRequest request);

    /**
     * 用人单位同意人力拒绝任务并再次派发
     * @param request
     * @return
     */
    ResultDO hotelAgreeAndSendTask(CreateTaskRequest request);

    /**
     * 查询当前用人单位任务数量
     * @param applyParamDTO
     * @return
     */
    int selectCurHotelTaskCount(ApplyParamDTO applyParamDTO);

    /**
     * 查询酒店任务
     * @param taskQueryDTO
     * @return
     */
    List<EmployerTask> queryHotelTask(TaskQueryDTO taskQueryDTO);
    /**
     * 用人单位同意人力报名申请并派发
     * @param createTaskRequest
     * @return
     */
    ResultDO agreeApplySendTask(CreateTaskRequest createTaskRequest);
    /**
     * 用人单位同意人力报名申请并派发
     * @param createTaskRequest
     * @return
     */
    ResultDO agreeApplyWorker(CreateTaskRequest createTaskRequest);

}
