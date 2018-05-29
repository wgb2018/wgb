package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Company;
import com.microdev.model.Message;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.MessageQuery;
import com.microdev.param.MessageShowDTO;

import java.util.List;
import java.util.Map;
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

    /**
     * 小时工绑定或解绑人力公司
     *
     * @param workerId
     * @param hrCompanyId
     * @param userName
     * @param pattern
     *            采用解绑的或绑定的模板
     * @return
     */
    void bindHrCompany(String workerId, Set<String> hrCompanyId, String userName, String pattern);

    /**
     * 酒店向人力公司派发任务
     * @param request
     * @param hotel
     * @param pattern
     * @return
     */
    void hotelDistributeTask(CreateTaskRequest request, Company hotel, String pattern);

    /**
     * 人力派发给小时工任务
     * @param list
     * @param hrId
     * @param hrName
     * @param pattern
     */
    void hrDistributeTask(List<Map<String, String>> list, String hrId, String hrName, String pattern);

    /**
     * 人力申请调配消息
     * @param c
     * @param reason
     * @param number
     * @param pattern
     */
    void sendMessage(TaskHrCompany c, String reason, String number, String pattern);

    /**
     * 查询未读消息
     * @param id
     * @param applyType
     * @return
     */
    List<Message> selectUnReadMessage(String id, String applyType);

    /**
     * 查询未读消息数量及各个类型的数量
     * @param id
     * @param applyType
     * @return
     */
    Map<String, Integer> selectUnReadCount(String id, String applyType);

    /**
     * 查询未读消息数量
     * @param id
     * @param applyType
     * @param type
     * @return
     */
    int selectMessageCount(String id, String applyType, int type);

    /**
     * 查询未处理的消息数量
     * @param id
     * @param applyType
     * @param status
     * @return
     */
    int selectUnHandleMessageAmount(String id, String applyType, int status);

    /**
     * 查询用户的消息
     * @param id           用户id
     * @param role         角色类型
     * @param type         显示消息类型
     * @param page         页码
     * @param pageNum      页数
     * @return
     */
    MessageShowDTO selectMessage(String id, int role, int type, int page, int pageNum);

}
