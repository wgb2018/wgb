package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Task;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.HotelPayHrRequest;
import com.microdev.param.TaskQueryDTO;

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
     * 酒店支付人力公司
     */
    ResultDO hotelPayHr(HotelPayHrRequest payHrRequest);

    /**
     * 查询酒店未读任务
     * @param hotelId
     * @return
     */
    int selectUnReadAmount(String hotelId);

    /**
     * 查询已完成任务数量
     * @param hotelId
     * @return
     */
    int selectCompleteAmount(String hotelId);

    /**
     * 更新任务的状态
     * @param taskId        任务id
     * @param status        1未完成已读3已完成已读
     */
    String updateTaskStatus(String taskId, Integer status);

    /**
     * 酒店再次派发任务
     * @param request
     * @return
     */
    ResultDO hotelAgainSendTask(CreateTaskRequest request);
}
