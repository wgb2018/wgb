package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskHrCompany;
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

    List<TaskHrCompany> queryHotelBill(String hotelId);

    List<TaskHrCompany> queryHrCompanyBill(String hrCompanylId);

    List<Map<String, Object>> selectPayHrInfo(String hotelId);

    Map<String, Object> selectByTaskId(String hrCompanyId);

    int selectUnreadCount(String hrCompanyId);

    int selectCompleteCount(String hrCompanyId);

    @Update("update task_hr_company set check_sign = #{status} where id = #{taskHrCompanyId}")
    int updateStatusById(String taskHrCompanyId, Integer status);
}

