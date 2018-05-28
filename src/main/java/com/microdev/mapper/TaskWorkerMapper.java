package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskWorker;
import com.microdev.param.TaskWorkerQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TaskWorkerMapper extends BaseMapper<TaskWorker> {
    TaskWorker findFirstById(String taskWorkerId);

    void save(TaskWorker taskWorker);

    void update(TaskWorker taskWorker);

    TaskWorker findWorkerNowTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime, @Param("Time") OffsetDateTime Time);

    TaskWorker findWorkerNextTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime);

    TaskWorker findWorkerBeforeTask(@Param("userId") String userId, @Param("status") int status, @Param("nowTime") OffsetDateTime nowTime);

    void addMinutes(@Param("taskWorkerId") String taskWorkerId, @Param("minutes") Long minutes, @Param("shouldPayMoney") Double shouldPayMoney);

    List<TaskWorker> findByUserId(String userId);

    List<TaskWorker> findAll(TaskWorkerQuery taskQueryDTO);

    List<TaskWorker> findByHrTaskId(String HrTaskId);

    List<TaskWorker> queryHrCompanyBill(String hrCompanyId);

    List<TaskWorker> queryWorkerBill(String workerId);

    Map<String, Object> selectHrId(String taskWorkerId);

    Map<String, Object> selectUserAndWorkerId(String id);

    List<Map<String, Object>> selectTaskWorkById(String taskHrId);
}
