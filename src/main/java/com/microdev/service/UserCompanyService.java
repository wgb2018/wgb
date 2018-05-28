package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.UserCompany;
import com.microdev.param.HrQueryWorkerDTO;
import com.microdev.param.WokerQueryHrDTO;
import java.util.List;
public interface UserCompanyService extends IService<UserCompany> {
    /**
     * 小时工绑定人力公司
     */
    ResultDO workerBindHr(String workerId, String hrId, String messageId);
    ResultDO workerUnbindHr(String workerId,String hrId);
    /**
     * 根据人力公司获取小时工
     */
    ResultDO getHrWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO);
    /**
     * 根据小时工获取人力公司
     */
    ResultDO getWorkerHrs(Paginator paginator, WokerQueryHrDTO queryDTO);
	ResultDO removeHrWorkers(List<String> hrTaskDistributeRequest);
}
