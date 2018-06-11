package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.TaskWorker;
import com.microdev.param.BillRequest;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.param.WorkerCancelTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TaskWorkerMapper extends BaseMapper<TaskWorker> {
    TaskWorker findFirstById(String taskWorkerId);

    void save(TaskWorker taskWorker);

    void update(TaskWorker taskWorker);

    TaskWorker findWorkerNowTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime, @Param("time") OffsetTime time, @Param("timeA") OffsetTime timeA);


    TaskWorker findWorkerNextTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime, @Param("time") OffsetTime time);

    TaskWorker findWorkerBeforeTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime);

    void addMinutes(@Param("taskWorkerId") String taskWorkerId, @Param("minutes") Long minutes, @Param("shouldPayMoney") Double shouldPayMoney);

    List<TaskWorker> findByUserId(String userId);

    List<TaskWorker> findAll(TaskWorkerQuery taskQueryDTO);

    List<TaskWorker> findByHrTaskId(String HrTaskId);

    List<TaskWorker> queryHrCompanyBill(BillRequest request);

    List<TaskHrCompany> queryWorkerBill(BillRequest request);

    Map<String, Object> selectHrId(String taskWorkerId);

    WorkerCancelTask selectUserAndWorkerId(String id);

    List<Map<String, Object>> selectTaskWorkById(String taskHrId);

    List<Map<String, Object>> selectTaskWorkCById(String taskHrId);

    int updateByPrimaryKey(Map<String, Object> map);

    int selectUnreadCount(String userId);

    int selectCompleteCount(String workerId);

    int selectWorkerUnreadCount(@Param("workerId") String workerId);

    void updateStatus(@Param("workerTaskId") String workerTaskId,@Param("status") Integer status);
}
