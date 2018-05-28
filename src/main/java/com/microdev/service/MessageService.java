package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.Message;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.MessageQuery;

import java.util.Set;

public interface MessageService extends IService<Message> {
    /**
     * 创建消息模板
     */
    ResultDO createMsgTemplate(CreateMsgTemplateRequest DTO);
    /**
     * 修改消息模板
     */
    ResultDO updateMsgTemplate(CreateMsgTemplateRequest DTO);
    /**
     * 消息模板详情
     */
    ResultDO getMsgTemplateByCode(String code);
    /**
     * 查询所有的消息模板
     */
    ResultDO getAllMsgTemplate();
    /**
     * 分页获取任务查询消息
     */
    ResultDO getPageMessages(Paginator paginator, MessageQuery query);
    /**
     * 设置消息已读
     */
    ResultDO updateMsgStatus(String id);
    /**
     * 酒店绑定或解绑人力公司 人力解绑或绑定酒店
     * @param bindCompany 要绑定的公司id的集合
     * @param applyCompany 申请绑定的公司
     * @param pattern   采用解绑的或绑定的模板
     * @param type  1  酒店添加(解绑)的人力公司2：人力公司添加(解绑)的酒店
     * @return
     */
    boolean hotelBindHrCompany(Set<String> bindCompany, Company applyCompany, String pattern, Integer type);
}
