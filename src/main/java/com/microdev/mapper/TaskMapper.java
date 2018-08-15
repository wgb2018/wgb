package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Task;
import com.microdev.model.TaskDateInfo;
import com.microdev.param.TaskHrCompanyViewDTO;
import com.microdev.param.TaskQueryDTO;
import com.microdev.param.TaskViewDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskMapper extends BaseMapper<Task> {
    void save(Task task);

    Task getFirstById(@Param("id") String id);

    List<Task> queryTasks(TaskQueryDTO task);

    void update(Task task);

    void addMinutes(@Param("taskId") String taskId, @Param("minutes") Long minutes, @Param("shouldPayMoney") Double shouldPayMoney);

    void updateStatus(@Param("taskId") String taskId, @Param("status") Integer status);

    Map<String, Double> selectHrAndTaskHourPay(String taskId);

    Map<String, Object> selectPayMoneyInfo(String hotelId);

    List<Map<String, Object>> selectHotelWaitDetails(Map<String, Object> map);

    TaskViewDTO findTaskAndHrInfoById(String id);

    TaskHrCompanyViewDTO selectTaskHrCompany(String taskId);

    @Select("SELECT th.task_id FROM (SELECT task_hr_id FROM task_worker WHERE id = #{taskWorkerId})tw INNER JOIN task_hr_company th ON tw.task_hr_id = th.id")
    String selectTaskIdByTaskWorkerId(String taskWorkerId);

    int queryHotelCurTaskCount(TaskQueryDTO queryDTO);

    int queryTasksCount(TaskQueryDTO dto);

    List<Map<String, Object>> selectStartTask();

    Integer selectStartTaskCount();
}
