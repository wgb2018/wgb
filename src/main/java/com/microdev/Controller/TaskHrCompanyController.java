package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.param.*;
import com.microdev.service.TaskHrCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
    public ResultDO accept(@RequestBody Map<String, String> map) {
        return ResultDO.buildSuccess(taskHrCompanyService.TaskHraccept(map.get("messageId")));
    }
    /**
     * 拒绝任务
     */
    @PostMapping("/hr-tasks/refuse")
    public ResultDO refuse(@RequestBody Map<String, String> map) {
        taskHrCompanyService.TaskHrrefuse(map.get("messageId"), map.get("reason"));
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
     * 分页查询人力公司的任务
     */
    @PostMapping("/hr-tasks/search")
    public ResultDO getPageData( @RequestBody PagingDO<TaskHrQueryDTO> paging) {
        return taskHrCompanyService.getPageTasks(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 人力公司结算小时工
     */
    @PostMapping("/hr/pay/worker")
    public ResultDO hrPayWorker(@RequestBody PayParam PayHrParam) throws Exception{
        return taskHrCompanyService.hrPayWorkers(PayHrParam);
    }
    /**
     * 人力公司申请调配
     */
    @PostMapping("/hr-tasks/swapWorker")

    public ResultDO swapWorker(@RequestBody Map<String, Object> map) {
        return taskHrCompanyService.hrApplyChangeWorker(map);
    }

    /**
     * PC端人力接受任务
     */
    @PostMapping("/hr-tasks/accept/pc")
    public ResultDO pcAccept(@RequestBody Map<String, String> param) {

        return ResultDO.buildSuccess(taskHrCompanyService.taskHracceptPC(param.get("id")));
    }

    /**
     * PC端人力拒绝任务
     */
    @PostMapping("/hr-tasks/refuse/pc")
    public ResultDO pcRefuse(@RequestBody Map<String, String> param) {
        return ResultDO.buildSuccess(taskHrCompanyService.TaskHrrefusePC(param.get("id"),param.get("reason")));
    }

    /**
     * 人力账单（查询用人单位支付账单）
     * @param response
     * @param taskHrQueryDTO
     */
    @PostMapping("/hr/download/account")
    public void downloadHrAccount(HttpServletResponse response, @RequestBody TaskHrQueryDTO taskHrQueryDTO) {
        List<DownLoadAccount> list = taskHrCompanyService.queryHrAccount(taskHrQueryDTO);
        ExcelUtil.download(response, list, ExcelUtil.hrAccount, "人力账单");
    }

    /**
     * 用人单位账单(查询支付给某个人力的账单)
     * @param response
     * @param taskHrQueryDTO
     */
    @PostMapping("/hotel/download/account")
    public void downloadHotelAccount(HttpServletResponse response, @RequestBody TaskHrQueryDTO taskHrQueryDTO) {
        List<DownLoadAccount> list = taskHrCompanyService.queryHotelAccount(taskHrQueryDTO);
        ExcelUtil.download(response, list, ExcelUtil.hotelAccount, "用人单位账单");
    }

    /**
     * 下载人力任务
     * @param response
     * @param taskHrQueryDTO
     */
    @PostMapping("/hr/task/download")
    public void downloadHrTask(HttpServletResponse response, @RequestBody TaskHrQueryDTO taskHrQueryDTO) {

        List<HrTask> list = taskHrCompanyService.queryHrTask(taskHrQueryDTO);
        ExcelUtil.download(response, list, ExcelUtil.hrTask, "人力任务");
    }
}
