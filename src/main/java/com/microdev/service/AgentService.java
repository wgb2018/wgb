package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Agent;
import com.microdev.param.AgentParam;
import com.microdev.param.AgentQureyParam;

public interface AgentService extends IService<Agent> {

    /**
     * 新增代理商账号
     * @param param
     * @return
     */
    public ResultDO addAgent(AgentParam param);

    /**
     * 修改代理商账号
     * @param param
     * @return
     */
    public ResultDO modifyAgent(AgentParam param);

    /**
     * 根据代理商登录名和密码查询
     * @param name
     * @param password
     * @return
     */
    public Agent selectByName(String name, String password);

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
}
