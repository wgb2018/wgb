package com.microdev.Controller;


import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.EmployerTask;
import com.microdev.param.PayParam;
import com.microdev.param.TaskQueryDTO;
import com.microdev.service.TaskService;
import com.microdev.service.TaskWorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用人单位任务相关的Api
 */
@RestController
public class TaskController {

    @Autowired
    TaskService taskService;
    @Autowired
    TaskWorkerService taskWorkerService;

    /**
     * 发布用人单位任务
     */
    @PostMapping("/tasks")
    public ResultDO createCompany(@RequestBody CreateTaskRequest createTaskRequest) {

        return taskService.createTask(createTaskRequest);
    }
    /**
     * 查询单个任务
     */
    @GetMapping("/tasks/{id}")
    public ResultDO getTaskById(@PathVariable String id) {

        return taskService.getTaskById(id);
    }
    /**
     * 分页查询用人单位任务
     */
    @PostMapping("/tasks/search")
    public ResultDO getPageData( @RequestBody PagingDO<TaskQueryDTO> paging) {

        return taskService.getPageTasks(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 用人单位按任务给人力公司结账(发送消息)
     */
    @PostMapping("/hotel/pay/hrCompany")
    public ResultDO payHr(@RequestBody PayParam PayHrParam) {
        return taskService.hotelPayHr(PayHrParam);
    }
    /**
     * 用人单位设置人力公司下哪个任务下哪个小时工违约
     */
    @PutMapping("/admin/tasks/{workerTaskId}/no-promise/")
    public ResultDO NoPromise(@PathVariable String workerTaskId) {

        return taskWorkerService.noPromise(workerTaskId);
    }

    /**
     * 用人单位同意人力拒绝任务并再次派发
     * @param createTaskRequest
     * @return
     */
    @PostMapping("/tasks/again/hotelSend")
    public ResultDO hotelAgainSendTask(@RequestBody CreateTaskRequest createTaskRequest) {
        return taskService.hotelAgreeAndSendTask(createTaskRequest);
    }

    /**
     * 下载用人单位任务
     * @param response
     * @param taskQueryDTO
     */
    @PostMapping("/task/hotel/download")
    public void downloadHotelTask(HttpServletResponse response,@RequestBody TaskQueryDTO taskQueryDTO) {

        List<EmployerTask> list = taskService.queryHotelTask(taskQueryDTO);
        ExcelUtil.download(response, list, ExcelUtil.employerTask, "用人单位任务");
    }
}
