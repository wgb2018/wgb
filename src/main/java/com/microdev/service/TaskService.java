package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Task;
import com.microdev.param.ApplyParamDTO;
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
     * 酒店再次派发任务
     * @param request
     * @return
     */
    ResultDO hotelAgainSendTask(CreateTaskRequest request);

    /**
     * 酒店同意人力拒绝任务并再次派发
     * @param request
     * @return
     */
    ResultDO hotelAgreeAndSendTask(CreateTaskRequest request);

    /**
     * 查询当前酒店任务数量
     * @param applyParamDTO
     * @return
     */
    int selectCurHotelTaskCount(ApplyParamDTO applyParamDTO);
}
