package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.ExcelUtil;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Company;
import com.microdev.model.User;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import com.microdev.service.UserCompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 小时工和人力公司关系
 */
@RestController
public class WorkerCompanyController {

    @Autowired
    UserCompanyService userCompanyService;
    @Autowired
    CompanyService companyService;
    @Autowired
    UserMapper userMapper;

    /**
     * 小时工反馈人力公司绑定申请
     */
    @GetMapping("/workers/{messageId}/bind/{status}")
    public ResultDO bindHr(@PathVariable String messageId, @PathVariable String status) {
        return userCompanyService.workerBindHr(messageId, status);
    }

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
        paging.getSelector().setType (2);
        return userCompanyService.getWorkerHrs(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 获取员工下所有用人单位
     */
    @PostMapping("/workers-hotels/search")
    public ResultDO getWorkersHotel(@RequestBody PagingDO<WokerQueryHrDTO> paging) {
        paging.getSelector().setType (1);
        return userCompanyService.getWorkerHrs(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 小时工申请解绑人力公司
     */
    @Deprecated
    @GetMapping("/workers/{workId}/unbind/{hrId}")
    public ResultDO unbindHr(@PathVariable String workId,@PathVariable String hrId) {
        return userCompanyService.workerUnbindHr(workId,hrId);
    }
    /**
     * 小时工申请解绑酒店
     */
    @PostMapping("/worker/apply/unbind/hotel")
    public ResultDO unbindHotel(@RequestBody workerUnbindHotel request) {
        return userCompanyService.workerUnbindHotel(request.getWorkerId(),request.getHotelId (),request.getReason ());
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

    /**
     * 查询人力所有小时工的待审核信息
     * @return
     */
    @PostMapping("/hrCompany/examine/worker")
    public ResultDO selectExamineWorker(@RequestBody PagingDO<QueryCooperateRequest> page) {
        Paginator paginator = page.getPaginator();
        return userCompanyService.selectUserByHrId(page.getSelector().getId(), paginator.getPage(), paginator.getPageSize());
    }

    /**
     * 人力公司处理小时工绑定申请
     * @param messageId   消息id
     * @param status      0拒绝1同意
     * @return
     */
    @GetMapping("/hrcompany/{messageId}/bind/{status}")
    public ResultDO hrcompanyBindWorker(@PathVariable String messageId,@PathVariable String status) {
        return userCompanyService.hrRespondWorkerBind(messageId, status);
    }

    /**
     * 用人单位处理小时工绑定申请
     * @param messageId   消息id
     * @param status      0拒绝1同意
     * @return
     */
    @GetMapping("/hotel/{messageId}/bind/{status}")
    public ResultDO hotelBindWorker(@PathVariable String messageId,@PathVariable String status) {
        return userCompanyService.hotelRespondWorkerBind(messageId, status);
    }

    /**
     * 工作者申请解绑人力
     * @param param
     * @return
     */
    @PostMapping("/worker/apply/unbind/hr")
    public ResultDO workerUnbindHr(@RequestBody Map<String, String> param) {

        return userCompanyService.workerApplyUnbindHr(param);
    }

    /**
     * 下载人力关联的工作者
     * @param response
     * @param queryDTO
     */
    @GetMapping("/hr/worker/download")
    public void downloadHrWorker(HttpServletResponse response,@ModelAttribute HrQueryWorkerDTO queryDTO) {

        List<WorkerCooperate> list = userCompanyService.queryHrBindWorker(queryDTO);
        String name = "";
        Company c = companyService.selectById(queryDTO.getHrId());
        if (c != null) {
            name = c.getName();
        }
        ExcelUtil.download(response, list, ExcelUtil.workerCooperate, "合作的小时工", name + "拥有的工作者");
    }

    /**
     * 下载小时工绑定的人力公司
     * @param response
     * @param queryDTO
     */
    @GetMapping("/worker/hr/download")
    public void workerBindHrDownload(HttpServletResponse response,@ModelAttribute WokerQueryHrDTO queryDTO) {

        List<CompanyCooperate> list = userCompanyService.queryWorkerBindHr(queryDTO);
        String name = "";
        User u = userMapper.selectByWorkerId(queryDTO.getWorkerId());
        if (u != null) {
            name = u.getNickname();
        }
        ExcelUtil.download(response, list, ExcelUtil.cooperate, "合作的人力公司", name + "加入的人力公司");
    }
}
