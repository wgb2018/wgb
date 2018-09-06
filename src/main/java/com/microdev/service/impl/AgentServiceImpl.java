package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.PasswordHash;
import com.microdev.mapper.AgentMapper;
import com.microdev.model.Agent;
import com.microdev.param.AgentAccountResponse;
import com.microdev.param.AgentParam;
import com.microdev.param.AgentQureyParam;
import com.microdev.service.AgentService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Transactional
@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

    @Autowired
    private AgentMapper agentMapper;
    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    /**
     * 新增代理商账号
     * @param param
     * @return
     */
    @Override
    public ResultDO addAgent(AgentParam param) {
        if (param == null || StringUtils.isEmpty(param.getName()) || StringUtils.isEmpty(param.getIdentifer()) || StringUtils.isEmpty(param.getPassword()) || param.getLevel() == null) {
            throw new ParamsException("参数不能为空");
        }

        Agent agent = selectByName(param.getName(), null);
        if (agent == null) {
            agent = new Agent();
            agent.setName(param.getName());
            try {
                agent.setPassword(PasswordHash.createHash(param.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("生成密码错误");
                throw new ParamsException("服务器出错");
            }
            agent.setIdentifer(param.getIdentifer());
            agent.setLevel(param.getLevel());
            agentMapper.insert(agent);
        } else {
            return ResultDO.buildError("用户名已存在");
        }
        return ResultDO.buildSuccess("创建成功");
    }

    /**
     * 修改代理商账号
     * @param param
     * @return
     */
    @Override
    public ResultDO modifyAgent(AgentParam param) {
        if (StringUtils.isEmpty(param.getId())) {
            throw new ParamsException("参数不能为空");
        }
        Agent agent = agentMapper.selectById(param.getId());
        if (agent == null) {
            return ResultDO.buildError("数据找不到");
        } else {
            agent.setLevel(param.getLevel());
            agent.setIdentifer(param.getIdentifer());
            agentMapper.updateById(agent);
        }
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 根据代理商登录名和密码查询
     * @param name
     * @param password
     * @return
     */
    @Override
    public Agent selectByName(String name, String password) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return agentMapper.selectByName(name, password);
    }

    /**
     * 修改代理商信息
     * @param param
     * @return
     */
    @Override
    public ResultDO updateAgentBaseInfo(AgentParam param) {
        if (param == null || StringUtils.isEmpty(param.getId())) {
            throw new ParamsException("参数不能为空");
        }
        Agent agent = agentMapper.selectById(param.getId());
        if (agent == null) {
            return ResultDO.buildError("查询不到代理商信息。");
        } else {
            transformBean(param, agent);
            agentMapper.updateById(agent);
        }
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 查询代理商账号信息
     * @param agentQureyParam
     * @return
     */
    @Override
    public ResultDO selectAgentAccountInfo(AgentQureyParam agentQureyParam, Paginator paginator) {

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<AgentAccountResponse> list = agentMapper.selectAccountByParam(agentQureyParam);
        Map<String, Object> result = new HashMap<>();
        PageInfo<AgentAccountResponse> pageInfo = new PageInfo<>(list);
        result.put("total", pageInfo.getTotal());
        result.put("page",paginator.getPage());
        result.put("result", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * 查询代理商基本信息
     * @param agentQureyParam
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectAgentBasicInfo(AgentQureyParam agentQureyParam, Paginator paginator) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<Agent> list = agentMapper.selectBasicByParam(agentQureyParam);
        Map<String, Object> result = new HashMap<>();
        PageInfo<Agent> pageInfo = new PageInfo<>(list);
        result.put("total", pageInfo.getTotal());
        result.put("page",paginator.getPage());
        result.put("result", list);
        return ResultDO.buildSuccess(result);
    }

    //转换bean
    private void transformBean(AgentParam param, Agent agent) {
        agent.setAgentName(param.getAgentName());
        agent.setLevel(param.getLevel());
        agent.setCompanyName(param.getCompanyName());
        agent.setAccountNumber(param.getAccountNumber());
        agent.setAddress(param.getAddress());
        agent.setArea(param.getArea());
        agent.setCity(param.getCity());
        agent.setProvince(param.getProvince());
        agent.setBank(param.getBank());
        agent.setCompanyEmail(param.getCompanyEmail());
        agent.setCompanyFax(param.getCompanyFax());
        agent.setCompanyProperty(param.getCompanyProperty());
        agent.setCreditCode(param.getCreditCode());
        agent.setIndustry(param.getIndustry());
        agent.setInvoicesTitle(param.getInvoicesTitle());
        agent.setLinkMan(param.getLinkMan());
        agent.setLinkPhone(param.getLinkPhone());
        agent.setMainBusiness(param.getMainBusiness());
        agent.setSubBranch(param.getSubBranch());
    }
}
