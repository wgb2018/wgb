package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.TaskWorker;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.RefusedTaskRequest;
import com.microdev.param.TaskWorkerQuery;

import java.util.Map;

public interface TaskWorkerService extends IService<TaskWorker> {
    /**
     * 设置违约的人员
     */
    ResultDO noPromise(String taskWorkerId);
    /**
     * 任务详情任务
     */
    ResultDO findWorkTaskById(String workerTaskId);
    /**
     * 接受任务
     */
    ResultDO receivedTask(String messageId);
    /**
     * 拒绝任务
     */
    ResultDO refusedTask(RefusedTaskRequest refusedTaskRequest);
    /**
     * 根据小时工获取任务详情列表
     */
    ResultDO pagesTaskWorkers(Paginator paginator, TaskWorkerQuery query);

    /**
     * 更新查看标识
     * @param taskWorkerId
     * @param status        状态1未完成已读3已完成已读
     * @return
     */
    String updateTaskWorkerStatus(String taskWorkerId, Integer status);

    /**
     * 查询小时工当前任务数量
     * @param applyParamDTO
     * @return
     */
    int selectWorkerCurTaskCount(ApplyParamDTO applyParamDTO);
}
