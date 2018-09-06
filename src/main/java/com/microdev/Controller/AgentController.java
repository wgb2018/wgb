package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.AgentParam;
import com.microdev.param.AgentQureyParam;
import com.microdev.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 创建代理商账号
     * @param agentParam
     * @return
     */
    @PostMapping("/agent/account/create")
    public ResultDO createAgent(@RequestBody AgentParam agentParam) {

        return agentService.addAgent(agentParam);
    }

    /**
     * 修改代理商账号
     * @param agentParam
     * @return
     */
    @PostMapping("/agent/account/modify")
    public ResultDO modifyAgent(@RequestBody AgentParam agentParam) {

        return agentService.modifyAgent(agentParam);
    }

    /**
     * 修改代理商信息
     * @param agentParam
     * @return
     */
    @PostMapping("/agent/info/update")
    public ResultDO updateAgentInfo(@RequestBody AgentParam agentParam) {

        return agentService.updateAgentBaseInfo(agentParam);
    }

    /**
     * 查询代理商账号信息
     * @param pagingDO
     * @return
     */
    @PostMapping("/agent/account/query")
    public ResultDO slectAgentAccountInfo(@RequestBody PagingDO<AgentQureyParam> pagingDO) {

        return agentService.selectAgentAccountInfo(pagingDO.getSelector(), pagingDO.getPaginator());
    }

    /**
     * 查询代理商基本信息
     * @param pagingDO
     * @return
     */
    @PostMapping("/agent/info/query")
    public ResultDO selectAgentBasicInfo(@RequestBody PagingDO<AgentQureyParam> pagingDO) {

        return agentService.selectAgentBasicInfo(pagingDO.getSelector(), pagingDO.getPaginator());
    }
}
