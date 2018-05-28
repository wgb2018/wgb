package com.microdev.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.WorkLog;
import com.microdev.param.PunchDTO;
import com.microdev.param.SupplementResponse;
import com.microdev.param.WorkerOneDayInfo;

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
}
