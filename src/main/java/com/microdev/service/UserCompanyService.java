package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.User;
import com.microdev.model.UserCompany;
import com.microdev.param.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserCompanyService extends IService<UserCompany> {
    /**
     * 小时工绑定人力公司
     */
    ResultDO workerBindHr( String messageId, String status);
    ResultDO workerUnbindHr(String workerId,String hrId);
    ResultDO workerUnbindHotel(String workerId,String hotelId,String reason);
    /**
     * 根据人力公司获取小时工
     */
    ResultDO getHrWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO);
    ResultDO getHrwWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO);
    /**
     * 根据小时工获取人力公司
     */
    ResultDO getWorkerHrs(Paginator paginator, WokerQueryHrDTO queryDTO);
	ResultDO removeHrWorkers(List<String> hrTaskDistributeRequest);

    /**
     *人力申请绑定用人单位
     * @param hrId
     * @param set
     * @return
     */
    String hrApplyBindWorker(String hrId, Set<String> set);

    /**
     * 查询对人力发出申请的小时工的待审核信息
     * @param hrCompanyId
     * @param page
     * @param pageNum
     * @return
     */
    ResultDO selectUserByHrId(String hrCompanyId, Integer page, Integer pageNum);

    /**
     * 人力查询合作的小时工
     * @param param
     * @param page
     * @param pageNum
     * @return
     */
    ResultDO selectWorkerCooperate(QueryCooperateRequest param, Integer page, Integer pageNum);

    /**
     * 人力公司处理小时工绑定申请
     * @param messageId
     * @param status
     * @return
     */
    ResultDO hrRespondWorkerBind(String messageId, String status);

    /**
     * 用人单位处理小时工绑定申请
     * @param messageId
     * @param status
     * @return
     */
    ResultDO hotelRespondWorkerBind(String messageId, String status);

    /**
     * 工作者申请解绑人力
     * @param param
     * @return
     */
    ResultDO workerApplyUnbindHr(Map<String, String> param);

    /**
     * 查询小时工绑定的人力公司
     * @param queryDTO
     * @return
     */
    List<CompanyCooperate> queryWorkerBindHr(WokerQueryHrDTO queryDTO);

    /**
     * 人力查询关联的工作者
     * @param queryDTO
     * @return
     */
    List<WorkerCooperate> queryHrBindWorker(HrQueryWorkerDTO queryDTO);
}
