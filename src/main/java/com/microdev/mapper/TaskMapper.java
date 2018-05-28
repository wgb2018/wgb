package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Task;
import com.microdev.param.TaskQueryDTO;
import com.microdev.param.TaskViewDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskMapper extends BaseMapper<Task> {
    void save(Task task);

    Task getFirstById(String id);

    List<Task> queryTasks(TaskQueryDTO task);

    void update(Task task);

    void addMinutes(String taskId, Long minutes, Double shouldPayMoney);

    void updateStatus(@Param("taskId") String taskId, @Param("status") Integer status);

    Map<String, Double> selectHrAndTaskHourPay(String taskId);

    Map<String, Object> selectPayMoneyInfo(String hotelId);

    List<Map<String, Object>> selectHotelWaitDetails(Map<String, Object> map);

    TaskViewDTO findTaskAndHrInfoById(String id);

}
