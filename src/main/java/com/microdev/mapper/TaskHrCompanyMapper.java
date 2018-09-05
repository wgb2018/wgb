package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.TaskWorker;
import com.microdev.param.BillRequest;
import com.microdev.param.HrTaskDetails;
import com.microdev.param.TaskHrQueryDTO;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskHrCompanyMapper extends BaseMapper<TaskHrCompany> {
    void save(TaskHrCompany taskHrCompany);

    List<TaskHrCompany> queryByHotelTaskId(@Param("id") String id);

    TaskHrCompany queryByTaskId(@Param("id") String id);

    void update(TaskHrCompany taskHrCompany);

    List<TaskHrCompany> queryHrCompanyTasks(TaskHrQueryDTO task);

    void addMinutes(@Param("taskHrCompanyId") String taskHrCompanyId, @Param("minutes")Long
            minutes, @Param("hrShouldPayMoney")Double hrShouldPayMoney, @Param("hotelShouldPayMoney")Double hotelShouldPayMoney);

    void updateStatus(@Param("id")String id, @Param("status")Integer status);

    List<TaskHrCompany> queryHotelBill(BillRequest request);

    List<TaskWorker> queryHotelBillWorker(BillRequest request);

    List<TaskHrCompany> queryHrCompanyBill(BillRequest request);

    List<Map<String, Object>> selectPayHrInfo(String hotelId);

    HrTaskDetails selectByTaskId(String hrCompanyId);

    int queryHrCurTaskCount(TaskHrQueryDTO queryDTO);

    List<TaskHrCompany> selectWorkHrTask(@Param("hotelId") String hotelId);

    int queryByHotelIdAndHrId(@Param ("hotelId") String hotelId,@Param ("hrId") String hrId);

    int queryHrCompanyTasksCount(TaskHrQueryDTO dto);
}

