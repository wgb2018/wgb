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
}
