package com.microdev.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.WorkLog;
import com.microdev.param.HotelHandleWorkerRecord;
import com.microdev.param.PunchDTO;
import com.microdev.param.SupplementResponse;
import com.microdev.param.WorkerOneDayInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface WorkerLogMapper extends BaseMapper<WorkLog> {

	WorkLog findFirstByTaskWorkerId(String taskWorkId);
	
	List<WorkLog> findByTaskWorkId(String taskWorkId);
	
	int updateByMapId(Map<String, Object> map);
	
	List<PunchDTO> countPunchInfo(Map<String, Object> map);
	
	Integer updateBatch(List<WorkLog> list);
	
	List<WorkLog> selectWorkLogByTime(Map<String, Object> map);
	
	int countPunchInfoNumber();
	
	List<SupplementResponse> selectNoPunchByWorkerId(String workerId);
	
	SupplementResponse selectNoPunchDetail(Map<String, Object> map);
	
	List<WorkerOneDayInfo> selectUserPunchDetail(String taskWorkerId);

	List<WorkLog> selectPunchDetails(String taskWorkerId);

	WorkLog selectUnreadInfoOne(@Param("taskWorkerId") String taskWorkerId,@Param("date") String date);

	List<String> selectUnreadPunchCount(@Param ("workerId") String workerId);

	@Update("update work_log set check_sign = 1 where task_worker_id = #{taskWorkerId} and DATE_FORMAT(create_time, '%Y/%m/%d') = #{date}")
	int updateCheckSign(String taskWorkerId, String date);

	List<WorkLog> selectByDate(HotelHandleWorkerRecord record);
}
