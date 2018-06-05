package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.HrPayWorkerRequest;
import com.microdev.param.HrTaskDistributeRequest;
import com.microdev.param.TaskHrQueryDTO;
import com.microdev.service.TaskHrCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人力公司任务相关的Api
 */
@RestController
public class TaskHrCompanyController {
    @Autowired
    TaskHrCompanyService taskHrCompanyService;
    /**
     * 人力资源公司查询单个任务
     */
    @GetMapping("/hr-tasks/{id}")
    public ResultDO getTaskHrCompanyById(@PathVariable String id) {
        return taskHrCompanyService.getTaskHrCompanyById(id);
    }
    /**
     * 接受任务
     */
    @PostMapping("/hr-tasks/accept")
    public ResultDO accept(String id, String messageId) {
        taskHrCompanyService.TaskHraccept(id, messageId);
        return ResultDO.buildSuccess("接受任务成功");
    }
    /**
     * 拒绝任务
     */
    @PostMapping("/hr-tasks/refuse")
    public ResultDO refuse(String id, String messageId, String reason) {
        taskHrCompanyService.TaskHrrefuse(id, messageId, reason);
        return ResultDO.buildSuccess("拒绝任务成功");
    }
    /**
     * 派发任务
     */
    @PostMapping("/hr-tasks/distribute")
    public ResultDO createCompany(@RequestBody HrTaskDistributeRequest request) {
        return taskHrCompanyService.TaskHrDistribute(request);
    }
    /**
     * 申请调配
     */
    @PostMapping("/hr-tasks/allocate/{id}")
    public ResultDO allocate(@PathVariable String id, String reason, Integer number) {
        taskHrCompanyService.TaskHrallocate(id, reason, number);
        return ResultDO.buildSuccess("申请任务调配成功");
    }
    /**
     * 分页查询人力公司的任务
     */
    @PostMapping("/hr-tasks/search")
    public ResultDO getPageData( @RequestBody PagingDO<TaskHrQueryDTO> paging) {
        return taskHrCompanyService.getPageTasks(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 人力公司结算小时工
     */
    @PutMapping("/admin/hr-tasks/{hrTaskId}/pay")
    public ResultDO createCompany(@PathVariable String hrTaskId,@RequestBody HrPayWorkerRequest payWorkerRequest) {
        payWorkerRequest.setHrTaskId(hrTaskId);
        return taskHrCompanyService.hrPayWorkers(payWorkerRequest);
    }
    /**
     * 人力公司申请调配
     */
    @PostMapping("/hr-tasks/swapWorker")
    public ResultDO swapWorker(Map<String, Object> map) {
        return taskHrCompanyService.hrApplyChangeWorker(map);
    }

    /**
     * 将任务更新为已查看
     * @param taskHrCompanyId
     * @param status
     * @return
     */
    @GetMapping("/hr-tasks/{taskHrCompanyId}/update/{status}")
    public ResultDO updateTaskHrStatus(@PathVariable String taskHrCompanyId,@PathVariable Integer status) {

        return ResultDO.buildSuccess(taskHrCompanyService.updateTaskHrStatus(taskHrCompanyId, status));
    }
}
