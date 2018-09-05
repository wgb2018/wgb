package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Worker;
import com.microdev.param.WorkerCooperate;
import com.microdev.param.WorkerInfo;
import com.microdev.param.WorkerQueryDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface WorkerMapper extends BaseMapper<Worker> {
    void save(Worker worker);

    Worker queryById(String workerId);
	Map<String,Object> queryWorker(String id);
    List<Map<String,Object>> queryAllWorker(WorkerQueryDTO workerQueryDTO);
	List<Map<String,Object>> queryWorkers(WorkerQueryDTO workerQueryDTO);
	List<Map<String,Object>> queryRecommendWorkers(WorkerQueryDTO workerQueryDTO);
	void updateStatus(@Param ("id") String id,@Param ("status") String status);

	Integer selectAllCount();
	List<WorkerInfo> selectWorkerInfo();
}

