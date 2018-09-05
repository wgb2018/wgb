package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.mapper.UserMapper;
import com.microdev.mapper.WorkerMapper;
import com.microdev.model.User;
import com.microdev.model.Worker;
import com.microdev.param.*;
import com.microdev.param.api.request.PunchRequest;
import com.microdev.param.api.response.GetCurrentTaskResponse;
import com.microdev.service.TaskWorkerService;
import com.microdev.service.WorkLogService;
import com.microdev.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
    private WorkerMapper workerMapper;
    @Autowired
    UserMapper userMapper;
    /**
     * 获取小时工当前任务信息
     */
    @GetMapping("/worker-tasks/{workerId}/current")
    public ResultDO getCurrentTask(@PathVariable String workerId) {
        GetCurrentTaskResponse g = workerService.getCurrentTask(workerId);
        if(g == null){
            return ResultDO.buildSuccess("无工作任务");
        }else{
            return ResultDO.buildSuccess(g);
        }

    }
    /**
     * 小时工打卡
     */
    @PostMapping("/worker/punch")
    public ResultDO punch(@RequestBody PunchRequest request) {

        request.setPunchTime (OffsetDateTime.now ());
        return ResultDO.buildSuccess(workerService.punch(
                request.getTaskWorkerId(), request.getPunchType(), OffsetDateTime.now(), request.getMeasure ()));
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
     * 分页获取小时工下的所有任务
     */
    @PostMapping("/workers/taskworkers")
    public ResultDO getPageTaskWorker(@RequestBody PagingDO<TaskWorkerQuery> paging) {
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
    @PostMapping("/workers/noPunch/info")
    public ResultDO noPunchPageInfo(@RequestBody PagingDO<ApplyParamDTO> paging) {
        return ResultDO.buildSuccess(workerService.selectNoPunchPageInfo(paging.getSelector(), paging.getPaginator()));
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
    /**
     * 小时工申请绑定用人单位
     * @return
     */
    @PostMapping("/worker/apply/bindHotels")
    public ResultDO workerApplyBindHotel(@RequestBody Map<String, Object> map) {

        return ResultDO.buildSuccess(workerService.workerApplybind((String) map.get("workerId"), (List<String>)map.get("set")));
    }

    /**
     * 小时工处理收入确认
     * @param messageId
     * @param status        0拒绝1同意
     * @return
     */
    @GetMapping("/worker/{messageId}/account/{status}")
    public ResultDO workerHandleAccount(@PathVariable String messageId,@PathVariable String status) {

        return workerService.workerHandleHrPay(messageId, status);
    }

    @GetMapping("/update/Worker/{id}/Status/{status}")
    public ResultDO updateStatus(@PathVariable String id,@PathVariable String status) {
        workerMapper.updateStatus(id,status);
        return ResultDO.buildSuccess ("更新成功");
    }

    /**
     * 下载工作者任务
     * @param response
     * @param taskQueryDTO
     */
    @GetMapping("/worker/task/download")
    public void downloadWorkerTask(HttpServletResponse response,@ModelAttribute TaskWorkerQuery taskQueryDTO) {

        List<WorkerTask> list = taskWorkerService.queryWorkerTask(taskQueryDTO);
        String name = "";
        User u = userMapper.selectByWorkerId(taskQueryDTO.getWorkerId());
        if (u != null) {
            name = u.getNickname();
        }
        ExcelUtil.download(response, list, ExcelUtil.workerTask, "工作者任务", name + "任务");
    }

    /**
     * 下载所有工作者信息
     * @param response
     */
    @GetMapping("/worker/info/download")
    public void downloadWorkerInfo(HttpServletResponse response) {

        List<WorkerInfo> list = workerService.queryWorkerInfo();
        ExcelUtil.download(response, list, ExcelUtil.workerInfo, "小时工信息", "小时工信息");
    }
}
