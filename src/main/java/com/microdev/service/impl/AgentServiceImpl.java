package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.PasswordHash;
import com.microdev.mapper.AgentMapper;
import com.microdev.mapper.AgentPowerMapper;
import com.microdev.mapper.ModelMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Agent;
import com.microdev.model.AgentPower;
import com.microdev.model.Model;
import com.microdev.model.User;
import com.microdev.param.AgentAccountRegister;
import com.microdev.param.AgentAccountResponse;
import com.microdev.param.AgentParam;
import com.microdev.param.AgentQureyParam;
import com.microdev.service.AgentService;
import com.microdev.type.UserSex;
import com.microdev.type.UserType;
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
    @Autowired
    private AgentPowerMapper agentPowerMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    /**
     * 新增代理商账号
     * @param param
     * @return
     */
    @Override
    public ResultDO addAgent(AgentAccountRegister param) {
        if (param == null || StringUtils.isEmpty(param.getName()) || StringUtils.isEmpty(param.getIdentifer()) || StringUtils.isEmpty(param.getPassword()) || StringUtils.isEmpty(param.getRoleId())) {
            throw new ParamsException("参数不能为空");
        }

        com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();
        User u = userMapper.selectById(user.getId());
        if (u.getUserType() != UserType.agant && u.getUserType() != UserType.platform)
            return ResultDO.buildSuccess("没有权限");
        if (userMapper.findByMobile(param.getName()) != null) {
            throw new ParamsException("手机号码已经存在");
        }
        User newUser = new User();
        newUser.setUsername(param.getName());
        newUser.setPassword(param.getPassword());
        newUser.setMobile(param.getName());
        newUser.setUserCode(UserType.agant.toString());
        newUser.setUserType(UserType.agant);
        newUser.setSex (UserSex.UNKNOW);
        if (org.springframework.util.StringUtils.hasText(param.getPassword())) {
            try {
                newUser.setPassword(PasswordHash.createHash(param.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
                return ResultDO.buildError("数据生成错误");
            }
        }
        newUser.setUsername(param.getName());
        userMapper.insert(newUser);
        Agent agent = selectByName(param.getIdentifer(), null);
        if (agent == null) {
            agent = new Agent();
            Agent parentAgent = selectByName(null, user.getId());
            if (parentAgent == null)
                return ResultDO.buildError("无权限");
            agent.setParentIdentifer(parentAgent.getIdentifer());
            agent.setIdentifer(param.getIdentifer());
            agent.setUserId(newUser.getPid());
            agentMapper.insert(agent);

            AgentPower agentPower = new AgentPower();
            agentPower.setAgentId(agent.getPid());
            agentPower.setPowerId(param.getRoleId());
            agentPowerMapper.insert(agentPower);


        } else {
            return ResultDO.buildError("代理商编号已存在");
        }
        return ResultDO.buildSuccess("创建成功");
    }

    /**
     * 修改代理商账号
     * @param param
     * @return
     */
    @Override
    public ResultDO modifyAgent(AgentAccountRegister param) throws Exception {
        if (StringUtils.isEmpty(param.getId())) {
            throw new ParamsException("参数不能为空");
        }
        User user = userMapper.selectById(param.getId());
        if (user == null)
            return ResultDO.buildError("查询不到该用户");
        if (StringUtils.isNotEmpty(param.getPassword())) {
            user.setPassword(PasswordHash.createHash(param.getPassword()));
            userMapper.updateById(user);
        }

        Agent agent = agentMapper.selectById(param.getId());
        if (agent == null) {
            return ResultDO.buildError("数据找不到");
        } else {
            List<String> list = agentMapper.selectOnePower(param.getId());
            if (list != null && list.size() > 0) {
                if (!list.contains(param.getRoleId())) {
                    String powerId = list.get(0);
                    agentPowerMapper.deleteByAgentId(param.getId());
                    AgentPower agentPower = new AgentPower();
                    agentPower.setAgentId(param.getId());
                    agentPower.setPowerId(powerId);
                    agentPowerMapper.insert(agentPower);
                }
            }

        }
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 根据代理商编号和用户id查询
     * @param identifer
     * @param userId
     * @return
     */
    @Override
    public Agent selectByName(String identifer, String userId) {
        if (StringUtils.isEmpty(identifer) && StringUtils.isEmpty(userId)) {
            return null;
        }
        return agentMapper.selectByName(identifer, userId);
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

    /**
     * 查询模块信息
     * @param userId
     * @return
     */
    @Override
    public List<Model> selectModelInfo(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (user.getUserType() == UserType.platform) {
            return modelMapper.selectAll();
        } else if (user.getUserType() == UserType.agant) {
            Agent agent = selectByName(null, user.getPid());
            return modelMapper.selectByAgentId(agent.getPid());
        }
        return null;
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
