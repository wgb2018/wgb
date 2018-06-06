package com.microdev.Controller;


import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.model.Task;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.HotelPayHrRequest;
import com.microdev.param.TaskQueryDTO;
import com.microdev.service.TaskService;
import com.microdev.service.TaskWorkerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 酒店任务相关的Api
 */
@RestController
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    TaskService taskService;
    @Autowired
    TaskWorkerService taskWorkerService;

    /**
     * 发布酒店任务
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
     * 分页查询酒店任务
     */
    @PostMapping("/tasks/search")
    public ResultDO getPageData( @RequestBody PagingDO<TaskQueryDTO> paging) {
        log.info("getPageData:" + paging.toString());
        return taskService.getPageTasks(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 酒店按任务给人力公司结账
     */
    @PutMapping("/admin/tasks/{taskId}/pay")
    public ResultDO payHr(@PathVariable String taskId,@RequestBody HotelPayHrRequest payHrRequest) {
        payHrRequest.setTaskId(taskId);
        log.info("payHr:" + taskId + ";" + payHrRequest.toString());
        return taskService.hotelPayHr(payHrRequest);
    }
    /**
     * 酒店设置人力公司下哪个任务下哪个小时工违约
     */
    @PutMapping("/admin/tasks/{workerTaskId}/no-promise/")
    public ResultDO NoPromise(@PathVariable String workerTaskId) {
        log.info("NoPromise:" + workerTaskId);
        return taskWorkerService.noPromise(workerTaskId);
    }

    /**
     * 更新任务查看状态
     * @param taskId
     * @param status
     * @return
     */
    @GetMapping("/tasks/{taskId}/update/{status}")
    public ResultDO updateTaskStatus(@PathVariable String taskId,@PathVariable Integer status) {
        return ResultDO.buildSuccess(taskService.updateTaskStatus(taskId, status));
    }

    /**
     * 酒店再次派发任务
     * @param createTaskRequest
     * @return
     */
    @PostMapping("/tasks/again/hotelSend")
    public ResultDO hotelAgainSendTask(@RequestBody CreateTaskRequest createTaskRequest) {

        return taskService.hotelAgainSendTask(createTaskRequest);
    }
}
