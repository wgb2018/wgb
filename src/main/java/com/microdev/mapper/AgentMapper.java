package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Agent;
import com.microdev.param.AgentAccountResponse;
import com.microdev.param.AgentQureyParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentMapper extends BaseMapper<Agent> {

    Agent selectByName(@Param("identifer") String identifer, @Param("userId") String userId);

    List<AgentAccountResponse> selectAccountByParam(AgentQureyParam agentQureyParam);

    List<Agent> selectBasicByParam(AgentQureyParam agentQureyParam);

    List<String> selectOnePower(@Param("agentId") String agentId);
}
