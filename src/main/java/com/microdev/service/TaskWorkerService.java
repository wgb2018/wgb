package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.TaskWorker;
import com.microdev.param.*;

import java.util.List;
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
     * 查询小时工当前任务数量
     * @param applyParamDTO
     * @return
     */
    int selectWorkerCurTaskCount(ApplyParamDTO applyParamDTO);

    /**
     * 查询小时工账单
     * @param taskQueryDTO
     * @return
     */
    List<DownLoadAccount> queryWorkerAccount(TaskWorkerQuery taskQueryDTO);

    /**
     * 查询小时工任务
     * @param taskQueryDTO
     * @return
     */
    List<WorkerTask> queryWorkerTask(TaskWorkerQuery taskQueryDTO);
}
