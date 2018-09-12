package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.AgentPower;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentPowerMapper extends BaseMapper<AgentPower> {

    @Delete("delete from agent_power where agent_id = #{agentId}")
    int deleteByAgentId(@Param("agentId") String agentId);
}
