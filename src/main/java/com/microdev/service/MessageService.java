package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.*;
import com.microdev.param.*;
import org.springframework.web.bind.annotation.PathVariable;

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
     * 用人单位绑定或解绑人力公司 人力解绑或绑定用人单位
     * @param bindCompany 要绑定的公司id的集合
     * @param applyCompany 申请绑定的公司
     * @param pattern   采用解绑的或绑定的模板
     * @param type  1  用人单位添加(解绑)的人力公司2：人力公司添加(解绑)的用人单位
     * @param reason 解绑原因
     * @return
     */
    String hotelBindHrCompany(Set<String> bindCompany, Company applyCompany, String pattern, Integer type, String reason);

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
    void bindHrCompany(String workerId, Set<String> hrCompanyId, String userName, String pattern, String reason);

    /**
     * 小时工解绑酒店
     *
     * @param workerId
     * @param hotelId
     * @param userName
     * @param pattern
     *            采用解绑的或绑定的模板
     * @return
     */
    void bindHotelCompany(String workerId, Set<String> hotelId, String userName, String pattern, String reason);

    /**
     * 用人单位向人力公司派发任务
     * @param set
     * @param hotel
     * @param pattern
     * @param request
     */
    void hotelDistributeTask(Set<TaskHrCompany> set, Company hotel, String pattern, CreateTaskRequest request);

    /**
     * 人力派发给小时工任务
     * @param list
     * @param hrId
     * @param hrName
     * @param pattern
     * @param taskId
     * @param hrTaskId
     */
    List<Message> hrDistributeTask(List<Map<String, String>> list, String hrId, String hrName, String pattern, String taskId, String hrTaskId,boolean isStop);

    /**
     * 人力申请调配消息
     * @param c
     * @param reason
     * @param number
     * @param pattern
     */
    void sendMessage(TaskHrCompany c, String reason, String number, String pattern);

    /**
     * 查询未读消息数量及各个类型的数量
     * @param id
     * @param applyType
     * @return
     */
    ResultDO selectUnReadCount(String id, String applyType);

    /**
     * 小时工绑定人力公司或人力公司绑定小时工
     * @param name
     * @param id
     * @param list
     * @param type    1小时工绑定人力2人力绑定小时工
     */
    void bindUserHrCompany(String name, String id, List<String> list, int type);

    /**
     * 分页显示用户的待处理数据
     * @param request
     * @param paginator
     * @return
     */
    ResultDO showWaitHandleWork(QueryCooperateRequest request, Paginator paginator);
    ResultDO showWaitHandleWorkPC(QueryCooperateRequest request, Paginator paginator);
    /**
     * 拒绝任务
     */
    void refuseTask(Map<String, String> param);

    /**
     * 查询消息明细
     * @param messageId   消息id
     * @param messagetype 用户类型小时工worker,人力hr用人单位hotel
     * @param type        消息类型
     * @return
     */
    MessageDetailsResponse selectMessageDetails(String messageId, String messagetype, String type);

    /**
     * 查询待处理事务详情中的任务信息
     * @param messageId         消息id
     * @param messagetype       用户类型小时工worker,人力hr用人单位hotel
     * @param type              消息类型
     * @return
     */
    AwaitTaskResponse selectAwaitTaskDetails(String messageId, String messagetype, String type);

    /**
     * 人力派发任务给小时工
     * @param list
     * @param taskHrCompany
     */
    Message hrDistributeWorkerTask(List<TaskWorker> list, TaskHrCompany taskHrCompany,boolean isStop);

    /**
     * 酒店任务给小时工
     * @param list
     * @param task
     */
    Message hotelDistributeWorkerTask(List<TaskWorker> list, Task task, boolean isStop);

    /**
     * 发送消息
     * @param param
     */
    Message sendMessageInfo(Map<String, Object> param);

    /**
     * 生成消息内容
     * @param param
     * @param pattern
     * @return
     */
    String installContent(Map<String, String> param, String pattern);

    /**
     * 查询申请调配信息
     * @param paging
     * @return
     */
    Map<String, Object> selectDeployApply(PagingDO<ApplyParamDTO> paging);

    /**
     * pc查询申请补签
     * @param paging
     * @return
     */
    ResultDO selectPcSupplement(PagingDO<ApplyParamDTO> paging);

    /**
     * pc查询请假申请
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcLeaveApply(ApplyParamDTO dto, Paginator paginator);

    /**
     * pc查询加班申请
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcExtraApply(ApplyParamDTO dto, Paginator paginator);

    /**
     * pc查询解绑申请
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcUnBindApply(ApplyParamDTO dto, Paginator paginator);

    /**
     * PC端查询绑定申请
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcBindApply(ApplyParamDTO dto, Paginator paginator);

    /**
     * Pc端查询人力拒绝接单
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcHrRefuseTask(ApplyParamDTO dto, Paginator paginator);

    /**
     * Pc端查询用人单位替换小时工
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcHotelReplace(ApplyParamDTO dto, Paginator paginator);

    /**
     * pc端查询用人单位支付
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcHotelPay(ApplyParamDTO dto, Paginator paginator);

    /**
     * PC端查询小时工取消任务
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcWorkerCancelTask(ApplyParamDTO dto, Paginator paginator);

    /**
     * pc端查询新任务
     * @param dto
     * @param paginator
     * @return
     */
    ResultDO selectPcHrNewTask(ApplyParamDTO dto, Paginator paginator);

    /**
     * pc端查询申请信息
     * @param id
     * @param roleType
     * @return
     */
    ResultDO selectPcApply(String id, String roleType);

    /**
     * 用人单位或人力处理解绑合作申请
     * @param messageId  消息id
     * @param status     0拒绝1同意
     * @return
     */
    ResultDO hotelHrHandleBind(String messageId, String status);

    /**
     * 用人单位同意小时工解绑申请
     * @param messageId  消息id
     * @param status     0拒绝1同意
     * @return
     */
    ResultDO hotelWorkerHandleBind(String messageId, String status);

    /**
     * 人力申请报名酒店任务
     * @param request
     * @param
     * @return
     */
    ResultDO hrApplyRegistration(AcceptNoticeRequest request);
    /**
     * 小时工申请报名酒店任务
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyHotel(AcceptNoticeRequest request);
    /**
     * 小时工申请报名人力任务
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyHr(AcceptNoticeRequest request);
    /**
     * 小时工申请报名人力招聘
     * @param request
     * @param
     * @return
     */
    ResultDO workerApplyRegistration(AcceptNoticeRequest request);
}
