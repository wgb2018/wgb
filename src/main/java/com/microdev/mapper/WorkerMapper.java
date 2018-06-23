package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Worker;
import com.microdev.param.WorkerQueryDTO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface WorkerMapper extends BaseMapper<Worker> {
    void save(Worker worker);

    Worker queryById(String workerId);
	Map<String,Object> queryWorker(String id);
    List<Map<String,Object>> queryAllWorker();
	List<Map<String,Object>> queryWorkers(WorkerQueryDTO workerQueryDTO);
}

