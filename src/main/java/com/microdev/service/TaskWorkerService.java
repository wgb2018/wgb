package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.TaskWorker;
import com.microdev.param.RefusedTaskRequest;
import com.microdev.param.TaskWorkerQuery;

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
    ResultDO receivedTask(String workerId, String workerTaskId);
    /**
     * 拒绝任务
     */
    ResultDO refusedTask(RefusedTaskRequest refusedTaskRequest);
    /**
     * 根据小时工获取任务详情列表
     */
    ResultDO pagesTaskWorkers(Paginator paginator, TaskWorkerQuery query);
}
