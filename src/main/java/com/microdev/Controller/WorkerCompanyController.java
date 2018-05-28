package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.HrQueryWorkerDTO;
import com.microdev.param.HrTaskDistributeRequest;import com.microdev.param.WokerQueryHrDTO;
import com.microdev.service.UserCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

/**
 * 小时工和人力公司关系
 */
@RestController
public class WorkerCompanyController {
    @Autowired
    UserCompanyService userCompanyService;
    /**
     * 小时工绑定人力公司
     */
    @PostMapping("/workers/{workId}/bind/{hrId}")
    public ResultDO bindHr(@PathVariable String workId, @PathVariable String hrId) {
        return userCompanyService.workerBindHr(workId,hrId);
    }
    /**
     * 小时工申请绑定人力公司
     */
    /*@PostMapping("/workers/bind")
    public ResultDO applyBindHr(@RequestBody RequestDO<String,Set<CompanyDTO>> param) {
        return userCompanyService.workerApplyBindHr(param.getWorkerId(),param.getCompanys());
    }*/
    /**
     * 获取人力公司下所有员工
     */
    @PostMapping("/hr-workers/search")
    public ResultDO getHrWorkers(@RequestBody PagingDO<HrQueryWorkerDTO> paging) {
        return userCompanyService.getHrWorkers(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 获取员工下所有人力公司
     */
    @PostMapping("/workers-hrs/search")
    public ResultDO getWorkersHr(@RequestBody PagingDO<WokerQueryHrDTO> paging) {
        return userCompanyService.getWorkerHrs(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 小时工解绑人力公司
     */
    @PutMapping("/workers/{workId}/unbind/{hrId}")
    public ResultDO unbindHr(@PathVariable String workId,@PathVariable String hrId) {
        return userCompanyService.workerUnbindHr(workId,hrId);
    }
	/**
     * 获取人力公司下已绑定任务的小时工
     */
    @PostMapping("/hrw-workers/search")
    public ResultDO getHrwWorkers(@RequestBody PagingDO<HrQueryWorkerDTO> paging) {
        return userCompanyService.getHrwWorkers(paging.getPaginator(),paging.getSelector());
    }

    /**
     * 人力公司移除已绑定任务的小时工
     */
    @PostMapping("/remove-workers/search")
    public ResultDO removeHrWorkers(@RequestBody List<String> workerTaskdelete) {
        return userCompanyService.removeHrWorkers(workerTaskdelete);
    }
}
