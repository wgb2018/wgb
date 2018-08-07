package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.mapper.TaskWorkerMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.mapper.WorkerMapper;
import com.microdev.model.TaskWorker;
import com.microdev.model.User;
import com.microdev.param.DownLoadAccount;
import com.microdev.param.RefusedTaskRequest;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.service.TaskWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 小时工任务相关的Api
 */
@RestController
public class TaskWorkerController {
    @Autowired
    TaskWorkerService taskWorkerService;
	@Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    WorkerMapper workerMapper;
    @Autowired
    UserMapper userMapper;
    /**
     * 小时工任务详情
     */
    @GetMapping("/worker-tasks/{workerTaskId}")
    public ResultDO getById(@PathVariable String workerTaskId) {
        return taskWorkerService.findWorkTaskById(workerTaskId);
    }
    /**
     * 小时工接受任务
     */
    @PostMapping("/worker-tasks/accept")
    public ResultDO workerAcceptTask(@RequestBody RefusedTaskRequest refusedReq) {
        return taskWorkerService.receivedTask(refusedReq.getMessageId());
    }
    /**
     * 小时工拒绝任务
     */
    @PostMapping("/worker-tasks/reject")
    public ResultDO getById(@RequestBody RefusedTaskRequest refusedReq) {
        return taskWorkerService.refusedTask(refusedReq);
    }
	/**
     * 根据人力公司任务ID查询小时工任务
     */
    @GetMapping("/worker-tasks/byHrTask/{id}")
    public ResultDO getworkerTasksByHrTask(@PathVariable String id) {
        List<TaskWorker> list = taskWorkerMapper.findByHrTaskId(id);
        for(int i = 0;i<list.size();i++){
            list.get(i).setUser(userMapper.selectById(list.get(i).getUserId()));
        }
        return ResultDO.buildSuccess(list);
    }

    /**
     * 下载小时工账单信息。
     * @param taskQueryDTO
     * @param response
     */
    @GetMapping("/worker/download/account")
    public void downloadWorkerAccount(@ModelAttribute TaskWorkerQuery taskQueryDTO, HttpServletResponse response) {
        List<DownLoadAccount> list = taskWorkerService.queryWorkerAccount(taskQueryDTO);
        String name = "";
        User u = userMapper.selectByWorkerId(taskQueryDTO.getWorkerId());
        if (u != null) {
            name = u.getNickname();
        }
        ExcelUtil.download(response, list, ExcelUtil.workerAccount, "小时工账单", name + "收款账单");
    }
}
