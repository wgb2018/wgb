package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Agent;
import com.microdev.model.Model;
import com.microdev.param.AgentAccountRegister;
import com.microdev.param.AgentParam;
import com.microdev.param.AgentQureyParam;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface AgentService extends IService<Agent> {

    /**
     * 新增代理商账号
     * @param param
     * @return
     */
    public ResultDO addAgent(AgentAccountRegister param);

    /**
     * 修改代理商账号
     * @param param
     * @return
     */
    public ResultDO modifyAgent(AgentAccountRegister param) throws Exception;

    /**
     * 根据代理商编号和用户id查询
     * @param identifer
     * @param userId
     * @return
     */
    public Agent selectByName(String identifer, String userId);

    /**
     * 修改代理商基本信息。
     * @param param
     * @return
     */
    public ResultDO updateAgentBaseInfo(AgentParam param);

    /**
     * 查询代理商账号信息
     * @param agentQureyParam
     * @return
     */
    public ResultDO selectAgentAccountInfo(AgentQureyParam agentQureyParam, Paginator paginator);

    /**
     * 查询代理商基本信息
     * @param agentQureyParam
     * @param paginator
     * @return
     */
    public ResultDO selectAgentBasicInfo(AgentQureyParam agentQureyParam, Paginator paginator);

    /**
     * 查询模块信息
     * @param userId
     * @return
     */
    public List<Model> selectModelInfo(String userId);
}
