package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Model;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelMapper extends BaseMapper<Model> {

    List<Model> selectByAgentId(@Param("agentId") String agentId);

    List<Model> selectAll();
}
