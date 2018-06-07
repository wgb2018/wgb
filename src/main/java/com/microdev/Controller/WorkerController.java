package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.param.*;
import com.microdev.param.api.request.PunchRequest;
import com.microdev.service.TaskWorkerService;
import com.microdev.service.WorkLogService;
import com.microdev.service.WorkerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 小时工相关的Api
 */
@RestController
public class WorkerController {
    @Autowired
    private WorkerService workerService;
    @Autowired
    private TaskWorkerService taskWorkerService;
    @Autowired
    private WorkLogService workLogService;

    /**
     * 获取小时工当前任务信息
     */
    @GetMapping("/worker-tasks/{workerId}/current")
    public ResultDO getCurrentTask(@PathVariable String workerId) {
        return ResultDO.buildSuccess(workerService.getCurrentTask(workerId));
    }
    /**
     * 小时工打卡
     */
    @PostMapping("/worker/punch")
    public ResultDO punch(@RequestBody PunchRequest request) {
        return ResultDO.buildSuccess(workerService.punch(
                request.getTaskWorkerId(), request.getPunchType(), OffsetDateTime.now()));
        //打卡时间取系统时间 避免客户端差异 原:request.getPunchTime()
    }
    /**
     * 获得小时工合作过的人力公司
     */
    @GetMapping("/workers/{userId}/hrcompanies")
    public ResultDO getHrCompanyPartners(@PathVariable String userId) {
        return ResultDO.buildSuccess(workerService.getHrCompanyPartners(userId));
    }
    /**
     * 获得小时工款项汇总信息
     */
    @GetMapping("/workers/{userId}/balance")
    public ResultDO getBalance(@PathVariable String userId) {
        return ResultDO.buildSuccess(workerService.getBalance(userId));
    }
    /**
     * 修改小时工姓名
     */
    /*@PutMapping("/workers/{userId}/name")
    public ResultDO updateName(@PathVariable String userId, @RequestBody UpdateNameRequest request) {
        return ResultDO.buildSuccess(workerService.update(userId, request.getName()));
    }*/
    /**
     * 分页获取小时工下的所有任务/付款信息
     */
    @PostMapping("/workers/{userId}/taskworkers")
    public ResultDO getPageTaskWorker(@PathVariable String userId, @RequestBody PagingDO<TaskWorkerQuery> paging) {
        paging.getSelector().setUserId(userId);
        return taskWorkerService.pagesTaskWorkers(paging.getPaginator(), paging.getSelector());
    }
    /**
     * 小时工申请请假
     */
    @PostMapping("/workers/leave")
    public ResultDO leave(@RequestBody WorkerSupplementRequest info) {
        return ResultDO.buildSuccess(workerService.askForLeave(info));
    }
    /**
     * 小时工申请加时
     */
    @PostMapping("/workers/workOvertime")
    public ResultDO workOvertime(@RequestBody WorkerSupplementRequest info) {
        return ResultDO.buildSuccess(workerService.askWorkOvertime(info));
    }
    /**
     * 小时工申请取消任务
     */
    @PostMapping("/workers/applyCancelTask")
    public ResultDO applyCancelTask(@RequestBody WorkerSupplementRequest info) {
        return ResultDO.buildSuccess(workerService.applyCancelTask(info));
    }
    /**
     * 小时工分页补签信息
     */
    @PostMapping("/workers/noPunchPageInfo")
    public ResultDO noPunchPageInfo(@RequestBody PageRequest page) {
        return ResultDO.buildSuccess(workerService.selectNoPunchPageInfo(page));
    }
    /**
     * 小时工补签详情
     */
    @PostMapping("/workers/noPunchInfoDetail")
    public ResultDO noPunchInfoDetail(String taskWorkerId, String date, String checkSign) {
        return ResultDO.buildSuccess(workerService.selectNoPunchDetails(taskWorkerId, date, checkSign));
    }
    /**
     * 小时工申请补签
     */
    @PostMapping("/workers/supplement")
    public ResultDO supplementWork(@RequestBody WorkerSupplementRequest info) {
        return ResultDO.buildSuccess(workerService.supplementWork(info));
    }
    /**
     * 查看小时工工作记录
     */
    @GetMapping("/workers/{taskWorkerId}/getWorkerDetails/{workerId}")
    public ResultDO getWorkerDetails(@PathVariable String taskWorkerId,@PathVariable String workerId) {
        return ResultDO.buildSuccess(workerService.selectUserTaskInfo(taskWorkerId, workerId));
    }
	/**
     * 修改服务类型及地区
     */
    @PostMapping("/modify/AreaAndService")
    public ResultDO mpdifyAreaAndService(@RequestBody AreaAndServiceRequest request) {
        workerService.mpdifyAreaAndService(request);
        return ResultDO.buildSuccess("修改成功");
    }
    /**
     * 获取小时工详情
     */
    @GetMapping("/query/worker/{id}")
    public ResultDO queryWorker(@PathVariable String id) {
        return ResultDO.buildSuccess(workerService.queryWorker(id));
    }

    /**
     * 将补签信息更新为已读
     * @param taskWorkerId
     * @param date
     * @return
     */
    @GetMapping("/workers/{taskWorkerId}/updateCheckSign/{date}")
    public ResultDO updateWorkerLogCheckSign(@PathVariable String taskWorkerId,@PathVariable String date) {

        return ResultDO.buildSuccess(workLogService.updateCheckSign(taskWorkerId, date));
    }
    /**
     * 分页查询小时工
     */
    @PostMapping("/workers/search")
    public ResultDO getPageData(@RequestBody PagingDO<WorkerQueryDTO> paging) {
        return workerService.pagingWorkers(paging.getPaginator(),paging.getSelector());
    }


    /**
     * 小时工申请绑定人力公司
     * @return
     */
    @PostMapping("/worker/apply/bindHrs")
    public ResultDO workerApplyBindHrCompany(@RequestBody Map<String, Object> map) {

        return ResultDO.buildSuccess(workerService.workerApplybind((String) map.get("workerId"), (List<String>)map.get("set")));
    }

}
