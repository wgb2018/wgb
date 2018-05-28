package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Worker;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface WorkerMapper extends BaseMapper<Worker> {
    void save(Worker worker);

    Worker queryById(String workerId);

    Map<String,Object> queryWorker(String id);
}

