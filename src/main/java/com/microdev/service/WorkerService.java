package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.Worker;
import com.microdev.param.*;
import com.microdev.param.AreaAndServiceRequest;
import com.microdev.param.api.response.GetBalanceResponse;
import com.microdev.param.api.response.GetCurrentTaskResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface WorkerService extends IService<Worker> {
    /**
     * 获取小时工当前任务
     *
     * @param workerId 小时工标识ID
     * @return 任务对象
     */
    GetCurrentTaskResponse getCurrentTask(String workerId);
    /**
     * 小时工打卡
     * @param taskWorkerId 小时工任务标识ID
     * @param punchType 打卡类型
     * @param punchTime 打卡时间(为保证给用户展示的时间与服务器存储的时间一致 将打卡时间传入 或可用来进行补卡操作)
     * @return
     */
    boolean punch(String taskWorkerId, PunchType punchType, OffsetDateTime punchTime);
    /**
     * 获取小时工合作过的人力公司
     *
     * @param workerId
     * @return 人力公司集合
     */
    List<Company> getHrCompanyPartners(String workerId);
    /**
     * 获取小时工款项信息
     * @param userId 小时工标识ID
     * @return
     */
    GetBalanceResponse getBalance(String userId);
    /**
     * 小时工申请请假
     * @param info
     * @return
     */
    boolean askForLeave(WorkerSupplementRequest info);
    /**
     * 小时工申请加班
     * @param info
     * @return
     */
    boolean askWorkOvertime(WorkerSupplementRequest info);
    /**
     * 小时工申请取消任务
     * @param info
     * @return
     */
    boolean applyCancelTask(WorkerSupplementRequest info);
    /**
     * 查询补签的记录
     * @param page
     * @return
     */
    PageInfo<SupplementResponse> selectNoPunchPageInfo(PageRequest page);
    /**
     * 查询补签记录详情
     * @param taskWorkerId
     * @param date
     * @param checkSign
     * @return
     */
    SupplementResponse selectNoPunchDetails(String taskWorkerId, String date, String checkSign);
    /**
     * 小时工补签
     * @param info
     * @return
     */
    boolean supplementWork(WorkerSupplementRequest info);
    /**
     * 查询小时工工作记录
     * @param taskWorkerId
     * @param userId
     * @return
     */
    UserTaskResponse selectUserTaskInfo(String taskWorkerId, String userId);

    /**
     * 查詢工作者信息
     * @param userId
     * @return
     */
    UserTaskResponse selectWorkerInfo(String userId);

	void mpdifyAreaAndService(AreaAndServiceRequest request);

    Map<String, Object> queryWorker(String id);

    ResultDO pagingWorkers(Paginator paginator, WorkerQueryDTO workerQueryDTO);
}
