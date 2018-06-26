package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;

@Transactional
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper,Message> implements MessageService{
    @Autowired
    MessageTemplateMapper messageTemplateMapper;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    private TaskWorkerMapper taskWorkerMapper;
    @Autowired
    private TaskHrCompanyService taskHrCompanyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private InformService informService;
    @Autowired
    private WorkerLogMapper workerLogMapper;
    @Autowired
    private TaskWorkerService taskWorkerService;

    /**
     * 创建消息模板
     */
    @Override
    public ResultDO createMsgTemplate(CreateMsgTemplateRequest msgTemplateRequest) {
        MessageTemplate messageTemplate  = messageTemplateMapper.findFirstByCode(msgTemplateRequest.getCode());
        if(messageTemplate!=null){
            throw new ParamsException("消息模板编码已存在:"+msgTemplateRequest.getCode());
        }
        messageTemplate=new MessageTemplate();
        messageTemplate.setContent(msgTemplateRequest.getContent());
        messageTemplate.setTitle(msgTemplateRequest.getTitle());
        messageTemplate.setCode(msgTemplateRequest.getCode());
        messageTemplate.setMsgLink(msgTemplateRequest.getMsgLink());
        messageTemplate.setDeleted(false);
        messageTemplateMapper.insert(messageTemplate);
        return ResultDO.buildSuccess(messageTemplate);
    }
    /**
     * 修改消息模板
     */
    @Override
    public ResultDO updateMsgTemplate(CreateMsgTemplateRequest msgTemplateRequest) {
        MessageTemplate messageTemplate =messageTemplateMapper.findFirstByCode(msgTemplateRequest.getCode());
        if(messageTemplate==null){
            throw new ParamsException("没有找到消息模板："+messageTemplate.getCode());
        }
        messageTemplate.setMsgLink(msgTemplateRequest.getMsgLink());
        messageTemplate.setTitle(msgTemplateRequest.getTitle());
        messageTemplate.setContent(msgTemplateRequest.getContent());
        messageTemplateMapper.updateById(messageTemplate);
        return ResultDO.buildSuccess(messageTemplate);
    }
    /**
     * 查询指定消息模板
     */
    @Override
    public ResultDO getMsgTemplateByCode(String code) {
        return ResultDO.buildSuccess(messageTemplateMapper.findFirstByCode(code));
    }
    /**
     * 查询所有的消息模板
     */
    @Override
    public ResultDO getAllMsgTemplate() {
        return ResultDO.buildSuccess(messageTemplateMapper.findAll());
    }
    //查询消息
    @Override
    public ResultDO getPageMessages(Paginator paginator, MessageQuery query) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Message> list = messageMapper.findAll(query);
        PageInfo<Message> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    /**
     * 酒店绑定或解绑人力公司 人力解绑或绑定酒店
     */
    @Override
    public String hotelBindHrCompany(Set<String> bindCompany, Company applyCompany, String pattern, Integer type) {
        System.out.println ("set:"+bindCompany);
        System.out.println ("applyCompany:"+applyCompany);
        System.out.println ("pattern:"+pattern);
        System.out.println ("type:"+type);
        if (bindCompany == null || bindCompany.size() == 0 || applyCompany == null) {
            throw new ParamsException("参数不能为空");
        }
        if (type != 1 && type != 2) {
            throw new ParamsException("参数type错误");
        }

        List<Message> list = new ArrayList<>();
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        System.out.println ("mess:"+mess);
        Iterator<String> it = bindCompany.iterator();
        Map<String, String> param = param = new HashMap<>();
        param.put("userName", applyCompany.getName());
        String content = StringKit.templateReplace(mess.getContent(), param);
        Message m = null;
        String id = null;
        while (it.hasNext()) {
            id = it.next();
            m = new Message();
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());

            m.setStatus(0);
            m.setMessageType(13);
            m.setIsTask(1);
            m.setContent(applyCompany.getName() + "向你发出了申请合作申请");
            if (type == 1) {
                m.setApplyType(2);
                m.setApplicantType(3);
                m.setHotelId(applyCompany.getPid());
                m.setHrCompanyId(id);
            } else {
                m.setApplyType(3);
                m.setApplicantType(2);
                m.setHotelId(id);
                m.setHrCompanyId(applyCompany.getPid());
            }

            m.setMessageContent(content);
            list.add(m);
        }
        messageMapper.saveBatch(list);
        return "操作成功";
    }

    /**
     * 小时工绑定
     * @param workerId
     * @param hrCompanyId
     * @param userName
     * @param pattern
     *            采用解绑的或绑定的模板
     * @return
     */
    @Override
    public void bindHrCompany(String workerId, Set<String> hrCompanyId, String userName, String pattern) {
        if (StringUtils.isEmpty(workerId) || hrCompanyId == null || hrCompanyId.size() == 0
                || StringUtils.isEmpty(userName) || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数不能为空");
        }

        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        if (mess == null) {
            throw new ParamsException("消息模板错误");
        }
        Iterator<String> it = hrCompanyId.iterator();
        Message m = null;
        List<Message> list = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        param.put("userName", userName);
        String c = StringKit.templateReplace(mess.getContent(), param);
        while (it.hasNext()) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            //m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            m.setApplicantType(1);
            m.setWorkerId(workerId);
            if ("applyBindMessage".equals(pattern)) {
                m.setMessageType(5);
                m.setContent(userName + "向您发出了申请绑定申请");
            } else {
                m.setMessageType(12);
                m.setContent(userName + "向您发出了申请解绑申请");
            }
            m.setMessageContent(c);
            m.setHrCompanyId(it.next());
            m.setApplyType(2);
            m.setIsTask(1);
            list.add(m);
        }
        messageMapper.saveBatch(list);
    }

    /**
     * 酒店向人力公司派发任务
     * @param set
     * @param hotel
     * @param pattern
     * @param request
     */
    @Override
    public void hotelDistributeTask(Set<TaskHrCompany> set, Company hotel, String pattern, CreateTaskRequest request) {
        if (request == null || hotel == null || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数不能为空");
        }
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        if (mess == null) {
            throw new ParamsException("模板查询不到");
        }
        Message m = null;
        List<Message> list = new ArrayList<>();
        //DateTimeFormatter format = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        //DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, String> param = new HashMap<>();
        param.put("hotelName", hotel.getName());
        param.put("taskContent", request.getTaskContent());

        String c = StringKit.templateReplace(mess.getContent(), param);
        for (TaskHrCompany dto : set) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(dto.getTaskTypeText());
            m.setStatus(0);
            m.setApplyType(2);
            m.setApplicantType(3);
            m.setMessageType(6);
            m.setHrCompanyId(dto.getHrCompanyId());
            m.setHotelId(hotel.getPid());
            m.setTaskId(dto.getTaskId());
            m.setMessageContent(c);
            m.setHrTaskId(dto.getPid());

            list.add(m);
        }
        messageMapper.saveBatch(list);
    }

    /**
     * 人力派发给小时工任务
     * @param list          小时工集合
     * @param hrId
     * @param hrName
     * @param pattern
     * @param taskId
     * @param hrTaskId
     */
    @Override
    public List<Message> hrDistributeTask(List<Map<String, String>> list, String hrId, String hrName, String pattern, String taskId, String hrTaskId,boolean isStop) {
        if (list == null || list.size() == 0 || StringUtils.isEmpty(hrId) || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数不能为空");
        }
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        if (mess == null) {
            throw new ParamsException("模板查询不到");
        }
        Message m = null;
        List<Message> messageList = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("hrCompanyName", hrName);
        String c = StringKit.templateReplace(mess.getContent(), map);
        for (Map<String, String> param : list) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            m.setMessageType(6);
            m.setApplyType(0);
            m.setApplicantType(2);
            m.setHrCompanyId(hrId);
            m.setWorkerId(param.get("workerId"));
            m.setWorkerTaskId(param.get("workerTaskId"));

            m.setTaskId(taskId);
            m.setIsTask(0);
            m.setHotelId(param.get("hotelId"));
            m.setHrTaskId(hrTaskId);
            m.setStop (isStop);
            m.setMessageContent(c);
            messageList.add(m);
        }
        messageMapper.saveBatch(messageList);
        return messageList;
    }

    /**
     * 人力申请调配消息
     * @param c
     * @param reason
     * @param number
     * @param pattern
     */
    @Override
    public void sendMessage(TaskHrCompany c, String reason, String number, String pattern) {
        if (c == null || StringUtils.isEmpty(reason) || StringUtils.isEmpty(number) || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数不能为空");
        }
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        if (mess == null) {
            throw new ParamsException("模板查询不到");
        }
        Message m = new Message();
        m.setDeleted(false);
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(c.getTaskTypeText());
        m.setStatus(0);
        m.setApplyType(3);
        m.setMessageType(4);
        m.setApplicantType(2);
        m.setIsTask(0);
        m.setHrCompanyId(c.getHrCompanyId());
        m.setHrTaskId (c.getPid ());
        m.setHotelId(c.getHotelId());
        m.setTaskId (c.getTaskId ());
        Map<String, String> map = new HashMap<>();
        map.put("hrCompanyName", c.getHrCompanyName());
        map.put("reason", reason);
        map.put("number", number);
        String str = StringKit.templateReplace(mess.getContent(), map);
        m.setMessageContent(str);
        messageMapper.insert(m);
    }


    /**
     * 查询未读消息数量及各个类型的数量
     * @param id            用户角色id
     * @param applyType     用户类型worker小时工hr人力公司hotel酒店
     * @return
     */
    @Override
    public Map<String, Integer> selectUnReadCount(String id, String applyType) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(applyType)) {
            throw new ParamsException("参数不能为空");
        }
        ApplyParamDTO applyParamDTO = new ApplyParamDTO();
        applyParamDTO.setId(id);
        Map<String, Integer> param = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        if ("worker".equals(applyType)) {
            //查询当前任务未读数量
            param.put("curTask", taskWorkerService.selectWorkerCurTaskCount(applyParamDTO));
            map.put("workerId", id);
            map.put("roleName", "worker");
            //查询未读待处理事物数量
            param.put("pendingTask", messageMapper.selectUnReadMessage(map));
            //查询未读通知数量
            map = new HashMap<>();
            map.put("status", 0);
            map.put("acceptType", 1);
            map.put("receiveId", id);
            param.put("notice", informService.selectCountByParam(map));

            //查询补签数量
            List<String> list = workerLogMapper.selectUnreadPunchCount(applyParamDTO.getId ());
            if (list == null) {
                param.put("supplement", 0);
            } else {
                param.put("supplement", list.size());
            }

        } else if ("hr".equals(applyType)) {

            param.put("curTask", taskHrCompanyService.selectHrCurTaskCount(applyParamDTO));
            map.put("hrId", id);
            map.put("roleName", "hr");
            param.put("pendingTask", messageMapper.selectUnReadMessage(map));
            map = new HashMap<>();
            map.put("status", 0);
            map.put("acceptType", 2);
            map.put("receiveId", id);
            param.put("notice", informService.selectCountByParam(map));

        } else if ("hotel".equals(applyType)) {

            param.put("curTask", taskService.selectCurHotelTaskCount(applyParamDTO));
            map.put("hotelId", id);
            map.put("roleName", "hotel");
            param.put("pendingTask", messageMapper.selectUnReadMessage(map));
            map = new HashMap<>();
            map.put("status", 0);
            map.put("acceptType", 3);
            map.put("receiveId", id);
            param.put("notice", informService.selectCountByParam(map));

        } else {
            throw new ParamsException("参数applyType类型错误");
        }

        return param;
    }

    /**
     * 小时工绑定人力公司或人力绑定小时工
     * @param name      申请人名称
     * @param id        申请绑定的用户id
     * @param list      待绑定的id列表
     * @param type      1小时工绑定人力2人力绑定小时工
     */
    @Override
    public void bindUserHrCompany(String name, String id, List<String> list, int type) {
        if (StringUtils.isEmpty(id) || list == null || list.size() == 0 ) {
            throw new ParamsException("参数错误");
        }
        if (type != 1 && type != 2) {
            throw new ParamsException("参数值错误");
        }
        Message message = null;
        MessageTemplate template = messageTemplateMapper.findFirstByCode("applyBindMessage");
        Map<String, String> map = new HashMap<>();
        map.put("userName", name);
        String str = StringKit.templateReplace(template.getContent(), map);
        List<Message> messageList = new ArrayList<>();
        for (String s : list) {
            message = new Message();
            message.setMessageContent(str);
            message.setMessageTitle(template.getTitle());
            message.setIsTask(1);
            message.setStatus(0);
            message.setMessageCode ("applyBindMessage");
            if (type == 1) {
                message.setWorkerId(id);
                message.setHrCompanyId(s);
                message.setApplicantType(1);
                message.setApplyType(2);
                message.setMessageType(5);
            } else {
                message.setApplyType(1);
                message.setApplicantType(2);
                message.setWorkerId(s);
                message.setHrCompanyId(id);
                message.setMessageType(5);
            }
            message.setContent(name + "向您发出了申请绑定申请");
            messageList.add(message);
        }
        messageMapper.saveBatch(messageList);
    }

    /**
     *分页查询待处理事务
     * @param request
     * @param paginator
     * @return
     */
    @Override
    public ResultDO showWaitHandleWork(QueryCooperateRequest request, Paginator paginator) {
        if (StringUtils.isEmpty(request.getId()) || StringUtils.isEmpty(request.getType())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<AwaitHandleInfo> list = null;
        if ("worker".equals(request.getType())) {
            list = messageMapper.selectWorkerAwaitHandleInfo(request.getId());
        } else if ("hotel".equals(request.getType())) {
            list = messageMapper.selectHotelAwaitHandleInfo(request.getId(),request.getMessageCode ());
            HrRefuseTaskNeedWorkers(list);
        } else if ("hr".equals(request.getType())) {
            list = messageMapper.selectHrAwaitHandleInfo(request.getId(),request.getMessageCode ());
        } else {
            throw new ParamsException("参数传递错误");
        }
        /*for (AwaitHandleInfo a:list) {
            a.setTaskResponse(this.selectAwaitTaskDetails(a.getMessageId(),  request.getType(),request.getMessageCode ().toString ()));
            a.setDetailsResponse (this.selectMessageDetails (a.getMessageId(),  request.getType(),request.getMessageCode ().toString ()));
        }*/
        PageInfo<AwaitHandleInfo> pageInfo = new PageInfo<>(list);
        Map<String, Object> map = new HashMap<>();
        map.put("page", pageInfo.getPageNum());
        map.put("total", pageInfo.getTotal());
        map.put("result", list);

        return ResultDO.buildSuccess(map);
    }

    @Override
    public ResultDO showWaitHandleWorkPC(QueryCooperateRequest request, Paginator paginator) {
        if (StringUtils.isEmpty(request.getId()) || StringUtils.isEmpty(request.getType())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<AwaitHandleInfo> list = null;
        if ("worker".equals(request.getType())) {
             list = messageMapper.selectWorkerAwaitHandleInfo(request.getId());
        } else if ("hotel".equals(request.getType())) {
            list = messageMapper.selectHotelAwaitHandleInfo(request.getId(),request.getMessageCode ());
            HrRefuseTaskNeedWorkers(list);
        } else if ("hr".equals(request.getType())) {
            list = messageMapper.selectHrAwaitHandleInfo(request.getId(),request.getMessageCode ());
        } else {
            throw new ParamsException("参数传递错误");
        }
        for (AwaitHandleInfo a:list) {
            a.setTaskResponse(this.selectAwaitTaskDetails(a.getMessageId(),  request.getType(),request.getMessageCode ().toString ()));
            a.setDetailsResponse (this.selectMessageDetails (a.getMessageId(),  request.getType(),request.getMessageCode ().toString ()));
        }
        //Map<String,Object> map = new HashMap<>();
        /*for(AwaitHandleInfo a:list){
            if(request.getMessageCode () == 7){//申请取消
                map.put ("taskTypeText",a.getTaskTypeText ());
                map.p
            }else if(request.getMessageCode () == 4){//申请调配

            }else if(request.getMessageCode () == 9){//申请换人

            }else if(request.getMessageCode () == 8){//收入确认

            }else if(){

            }else if(){

            }else if(){

            }else if(){

            }else if(){

            }else if(){

            }else if(){

            }else if(){

            }
        }*/
        PageInfo<AwaitHandleInfo> pageInfo = new PageInfo<>(list);
        Map<String, Object> map = new HashMap<>();
        map.put("page", pageInfo.getPageNum());
        map.put("total", pageInfo.getTotal());
        map.put("result", list);

        return ResultDO.buildSuccess(map);
    }

    /**
     * 拒绝任务(type:0小时工拒绝任务1人力拒绝任务)
     */
    @Override
    public void refuseTask(Map<String, String> param) {
        if (StringUtils.isEmpty(param.get("userName")) || StringUtils.isEmpty(param.get("startId")) || StringUtils.isEmpty(param.get("endId"))) {
            throw new ParamsException("参数错误");
        }
        Message message = null;
        MessageTemplate template = messageTemplateMapper.findFirstByCode("refuseTaskMessage");
        if (template == null) {
            throw new ParamsException("消息模板查询不到");
        }
        Map<String, String> map = new HashMap<>();
        map.put("userName", param.get("userName"));
        map.put("content", param.get("reason"));
        String str = StringKit.templateReplace(template.getContent(), map);
        message = new Message();
        message.setMessageContent(str);
        message.setMessageTitle(template.getTitle());
        message.setMessageCode(template.getCode());
        message.setStatus(0);
        message.setMessageType(10);
        message.setTaskId("");
        message.setContent(param.get("reason"));
        if ("0".equals(param.get("type"))) {
            message.setWorkerId(param.get("startId"));
            message.setHrCompanyId(param.get("endId"));
            message.setApplicantType(1);
            message.setApplyType(2);
        } else {
            message.setHrCompanyId(param.get("startId"));
            message.setHotelId(param.get("endId"));
            message.setApplicantType(2);
            message.setApplyType(3);
        }
        messageMapper.insert(message);
    }

    /**
     * 查询消息明细
     * @param messageId   消息id
     * @param messagetype 用户类型小时工worker,人力hr酒店hotel
     * @param type        消息类型
     * @return
     */
    @Override
    public MessageDetailsResponse selectMessageDetails(String messageId, String messagetype, String type) {

        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(type)) {
            throw new ParamsException("参数不能为空");
        }
        MessageDetailsResponse response = null;
        //根据消息id和类型查询待处理信息
        if ("12".equals(type)) {

            response = messageMapper.selectWorkerApply(messageId);
            if (response != null) {
                response.setOriginator(response.getName());
            }
        } else if ("13".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectCompanyApply(messageId);
            } else if ("hotel".equals(messagetype)) {
                response = messageMapper.hotelHrApplyCooperate(messageId);

            } else {
                throw new ParamsException("用户类型错误");
            }
            if (response != null) {
                response.setOriginator(response.getCompanyName());
            }
        } else if ("5".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectWorkerApply(messageId);
                if (response != null) {
                    response.setOriginator(response.getName());
                }
            } else if ("worker".equals(messagetype)) {
                response = messageMapper.hotelHrApplyCooperate(messageId);
                if (response != null) {
                    response.setOriginator(response.getCompanyName());
                }
            } else {
                throw new ParamsException("用户类型错误");
            }
        } else if ("8".equals(type)) {
            if ("worker".equals(messagetype)) {
                response = messageMapper.selectPayConfirm(messageId, "2");

            } else if ("hr".equals(messagetype)){
                response = messageMapper.selectPayConfirm(messageId, "1");
            }
            if (response != null) {
                response.setOriginator(response.getCompanyName());
            }

        } else if ("1".equals(type) ) {

            response = messageMapper.selectSupplementApply(messageId);
            if (response != null) {
                response.setOriginator(response.getName());
            }

        } else if ("2".equals(type)){

            response = messageMapper.selectOvertimeApply(messageId);
            if (response != null) {
                response.setOriginator(response.getName());
            }
        } else if ("3".equals(type)) {

            response = messageMapper.selectLeaveApply(messageId);
            if (response != null) {
                response.setOriginator(response.getName());
            }
        } else if ("4".equals(type)) {
            response = messageMapper.selectApplyAllocate(messageId);
            if (response != null) {
                response.setOriginator(response.getCompanyName());
            }

        } else if ("7".equals(type)) {
            response = messageMapper.selectWorkerApply(messageId);
            if (response != null) {
                response.setOriginator(response.getName());
            }
        } else if ("9".equals(type)) {
            response = messageMapper.selectWorkerApply(messageId);
            if (response != null) {
                String companyName = messageMapper.selectCompanyNameByMessageId(messageId);
                response.setOriginator(companyName);
            }
        } else if ("10".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectWorkerApply(messageId);
                if (response != null) {
                    response.setOriginator(response.getName());
                }

            } else if ("hotel".equals(messagetype)) {
                response = messageMapper.selectHotelApply(messageId);
                if (response != null) {
                    response.setOriginator(response.getCompanyName());
                }

            } else {
                throw new ParamsException("用户类型错误");
            }
        }
        if (response != null) {
            if (response.getAge() < 0) response.setAge(0);
            response.setMessageTextType(transMessageType(response.getMessageType()));
        }
        return response;
    }

    /**
     * 查询消息明细---任务信息
     * @param messageId         消息id
     * @param messagetype       用户类型小时工worker,人力hr酒店hotel
     * @param type              消息类型
     * @return
     */
    @Override
    public AwaitTaskResponse selectAwaitTaskDetails(String messageId, String messagetype, String type) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(messagetype) || StringUtils.isEmpty(type)) {
            throw new ParamsException("参数错误");
        }
        AwaitTaskResponse response = null;
        if ("6".equals(type)) {
            if ("worker".equals(messagetype)) {
                response = messageMapper.selectWorkerAwaitHandleTask(messageId);

            } else if ("hr".equals(messagetype)) {
                response = messageMapper.selectHrAwaitHandleTask(messageId);
            }
        } else if ("1".equals(type) || "2".equals(type) || "3".equals(type) || "4".equals(type) || "11".equals(type)) {
            response = messageMapper.selectHrAwaitHandleTask(messageId);
        } else if ("10".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectHrAwaitHandleTask(messageId);
            } else if ("hotel".equals(messagetype)) {
                response = messageMapper.selectWorkerAwaitHandleTask(messageId);
            }
        } else if ("7".equals(type)) {
            response = messageMapper.selectCancelApply(messageId);
        } else if ("8".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectHrHotelDetails(messageId);
            } else if ("worker".equals(messagetype)) {
                response = messageMapper.selectHrHotelDetails(messageId);
            } else {
                throw new ParamsException("参数错误");
            }
        } else if ("9".equals(type)) {
            response = messageMapper.selectHrHotelDetails(messageId);
        } else if ("10".equals(type)) {
            response = messageMapper.selectHrHotelDetails(messageId);
        } else if ("11".equals(type)) {
            response = messageMapper.selectHrHotelDetails(messageId);
        }
        if (response != null && "0".equals(response.getHourlyPay())) {
            response.setHourlyPay("");
        }
        return response;
    }

    /**
     * 人力派发任务给小时工
     * @param list
     * @param taskHrCompany
     */
    @Override
    public Message hrDistributeWorkerTask(List<TaskWorker> list, TaskHrCompany taskHrCompany,boolean isStop) {
        if (list == null || list.size() == 0 || taskHrCompany == null) {
            throw new ParamsException("消息发送的参数错误");
        }

        MessageTemplate template = messageTemplateMapper.findFirstByCode("workTaskMessage");
        if (template == null) {
            throw new ParamsException("找不到消息模板");
        }
        Map<String, String> param = new HashMap<>();
        param.put("hrCompanyName", taskHrCompany.getHrCompanyName());
        String str = StringKit.templateReplace(template.getContent(), param);
        Message message = null;
        for (TaskWorker worker : list) {
            message = new Message();
            message.setStatus(0);
            message.setMessageCode(template.getCode());
            message.setMessageTitle(taskHrCompany.getTaskTypeText());
            message.setContent(str);
            message.setMessageType (6);
            message.setMessageContent(str);
            message.setHrTaskId(taskHrCompany.getPid());
            message.setApplyType(0);
            message.setApplicantType(2);
            message.setHotelId(taskHrCompany.getHotelId());
            message.setHrCompanyId(taskHrCompany.getHrCompanyId());
            message.setIsTask(0);
            message.setWorkerId(worker.getWorkerId());
            message.setWorkerTaskId(worker.getPid());
            message.setTaskId (taskHrCompany.getTaskId ());
            message.setStop (isStop);
            messageMapper.insert(message);
        }
        return message;
    }

    /**
     * 发送消息
     * @param param
     */
    @Override
    public Message sendMessageInfo(Map<String, Object> param) {
        if (param == null) {
            throw new ParamsException("参数不能为空");
        }
        Message message = new Message();
        message.setStatus(0);
        message.setIsTask(0);
        if (param.get("hrCompanyId") != null)
            message.setHrCompanyId((String)param.get("hrCompanyId"));
        if (param.get("workerTaskId") != null)
            message.setWorkerTaskId((String)param.get("workerTaskId"));
        if (param.get("workerId") != null)
            message.setWorkerId((String)param.get("workerId"));
        if (param.get("hotelId") != null)
            message.setHotelId((String)param.get("hotelId"));
        message.setApplicantType((Integer)param.get("applicantType"));
        message.setApplyType((Integer)param.get("applyType"));
        message.setMessageContent((String)param.get("messageContent"));
        message.setContent((String)param.get("content"));
        message.setTaskId((String)param.get("taskId"));
        message.setMessageCode((String)param.get("messageCode"));
        message.setMessageTitle((String)param.get("messageTitle"));
        if (param.get("hrTaskId") != null)
            message.setHrTaskId((String)param.get("hrTaskId"));
        if (param.get("minutes") != null)
            message.setMinutes((String)param.get("minutes"));
        if (param.get("supplementTime") != null) {
            message.setSupplementTime((OffsetDateTime) param.get("supplementTime"));
        }
        if (param.get("supplementTimeEnd") != null)
            message.setSupplementTimeEnd((OffsetDateTime) param.get("supplementTimeEnd"));
        if (param.get("messageType") != null)
            message.setMessageType((Integer)param.get("messageType"));
        if (param.get("taskId") != null)
            message.setTaskId ((String)param.get("taskId"));
        messageMapper.insert(message);
        return message;
    }

    /**
     * 生成消息内容
     * @param param
     * @param pattern
     * @return
     */
    @Override
    public String installContent(Map<String, String> param, String pattern) {

        if (param == null || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数错误");
        }
        MessageTemplate template = messageTemplateMapper.findFirstByCode(pattern);
        if (template == null) {
            throw new ParamsException("查询不到消息模板.");
        }
        return StringKit.templateReplace(template.getContent(), param);
    }

    /**
     * 查询申请调配信息
     * @param paging
     * @return
     */
    @Override
    public Map<String, Object> selectDeployApply(PagingDO<ApplyParamDTO> paging) {
        ApplyParamDTO applyParamDTO = paging.getSelector();
        if (applyParamDTO == null || StringUtils.isEmpty(applyParamDTO.getId()) || StringUtils.isEmpty(applyParamDTO.getRoleType())) {
            throw new ParamsException("参数不能为空");
        }

        List<ApplyResponseDTO> list = null;
        PageHelper.startPage(paging.getPaginator().getPage(), paging.getPaginator().getPageSize(), true);
        if ("hotel".equals(applyParamDTO.getRoleType())) {
            list = messageMapper.selectHotelDeploy(applyParamDTO.getId());
        } else if ("hr".equals(applyParamDTO.getRoleType())) {
            list = messageMapper.selectHrDeploy(applyParamDTO.getId());
        } else {
            throw new ParamsException("用户类型错误");
        }

        Map<String, Object> result = new HashMap<>();
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return result;
    }

    /**
     * PC查询申请补签
     * @param paging
     * @return
     */
    @Override
    public ResultDO selectPcSupplement(PagingDO<ApplyParamDTO> paging) {

        if (paging == null || paging.getSelector() == null || StringUtils.isEmpty(paging.getSelector().getId())) {
            throw new ParamsException("参数错误");
        }
        Paginator paginator = paging.getPaginator();
        Map<String, Object> result = new HashMap<>();
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);

        List<ApplySupplementRequest> list = messageMapper.selectPcLeaveApply(paging.getSelector().getId(), "1");
        PageInfo<ApplySupplementRequest> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * pc查询请假申请
     * @param dto
     * @param paginator     分页参数
     * @return
     */
    @Override
    public ResultDO selectPcLeaveApply(ApplyParamDTO dto, Paginator paginator) {

        if (dto == null || StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        Map<String, Object> result = new HashMap<>();
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplySupplementRequest> list = messageMapper.selectPcLeaveApply(dto.getId(), "3");
        PageInfo<ApplySupplementRequest> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     *  pc查询加班申请
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcExtraApply(ApplyParamDTO dto, Paginator paginator) {

        if (dto == null || StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        Map<String, Object> result = new HashMap<>();
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplySupplementRequest> list = messageMapper.selectPcLeaveApply(dto.getId(), "2");
        PageInfo<ApplySupplementRequest> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     *pc查询解绑申请
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcUnBindApply(ApplyParamDTO dto, Paginator paginator) {

        if (dto == null || StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        Map<String, Object> result = new HashMap<>();
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<BindPcResponse> list = messageMapper.selectPcUnBindApply(dto.getId());
        PageInfo<BindPcResponse> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * PC端查询绑定申请
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcBindApply(ApplyParamDTO dto, Paginator paginator) {

        if (StringUtils.isEmpty(dto.getId()) || StringUtils.isEmpty(dto.getRoleType())) {
            throw new ParamsException("参数不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<ApplyBindResponse> list = null;
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        if ("hotel".equals(dto.getRoleType())) {
            list = messageMapper.selectPcHotelBind(dto.getId());
        } else if ("hr".equals(dto.getRoleType())) {
            list = messageMapper.selectPcHrBind(dto.getId());
        } else {
            throw new ParamsException("参数错误");
        }
        PageInfo<ApplyBindResponse> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * Pc端查询人力拒绝接单
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcHrRefuseTask(ApplyParamDTO dto, Paginator paginator) {
        if (StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数不能为空");
        }

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplyResponseDTO> list = messageMapper.selectPcHrRefuse(dto.getId());
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * Pc端查询酒店替换小时工
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcHotelReplace(ApplyParamDTO dto, Paginator paginator) {

        if (StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数不能为空");
        }

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplyResponseDTO> list = messageMapper.selectPcHotelReplace(dto.getId());
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * pc端查询酒店支付
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcHotelPay(ApplyParamDTO dto, Paginator paginator) {
        if (StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplyResponseDTO> list = messageMapper.selectPcHotelPay(dto.getId());
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * PC端查询小时工取消任务
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcWorkerCancelTask(ApplyParamDTO dto, Paginator paginator) {
        if (StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplyResponseDTO> list = messageMapper.selectPcworkerCancel(dto.getId());
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * pc端查询新任务
     * @param dto
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectPcHrNewTask(ApplyParamDTO dto, Paginator paginator) {
        if (StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<ApplyResponseDTO> list = messageMapper.selectPcHrNewTask(dto.getId());
        PageInfo<ApplyResponseDTO> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }

    /**
     * pc端查询申请消息
     * @param id
     * @param roleType   人力hr酒店hotel
     * @return
     */
    @Override
    public ResultDO selectPcApply(String id, String roleType) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(roleType)) {
            throw new ParamsException("参数不能为空");
        }
        List<MessageResponse> list = null;
        if ("hr".equals(roleType)) {
            list = messageMapper.selectPcHrApplyInfo(id);
        } else if ("hotel".equals(roleType)) {
            list = messageMapper.selectPcHotelApplyInfo(id);
        } else {
            throw new ParamsException("参数的值错误");
        }
        if (list == null || list.size() == 0) {
            list = new ArrayList<>();
        }
        return ResultDO.buildSuccess(list);
    }

    private String transMessageType(String messageType) {
        if (StringUtils.isEmpty(messageType)) return messageType;
        if ("1".equals(messageType)) {
            return "申请补签";
        } else if ("2".equals(messageType)) {
            return "申请加时";
        } else if ("3".equals(messageType)) {
            return "申请请假";
        } else if ("4".equals(messageType)) {
            return "申请调配";
        } else if ("5".equals(messageType)) {
            return "申请绑定";
        } else if ("6".equals(messageType)) {
            return "新任务";
        } else if ("7".equals(messageType)) {
            return "申请取消任务";
        } else if ("8".equals(messageType)) {
            return "收入确认";
        } else if ("9".equals(messageType)) {
            return "申请替换";
        } else if ("10".equals(messageType)) {
            return "拒绝接单";
        } else if ("11".equals(messageType)) {
            return "待派单";
        } else if ("12".equals(messageType)) {
            return "申请解绑";
        } else if ("12".equals(messageType)) {
            return "申请合作";
        }
        return messageType;
    }

    /**
     * 人力拒绝酒店任务或调配酒店任务时，所需人数为人力任务数
     * @param list
     */
    private void HrRefuseTaskNeedWorkers(List<AwaitHandleInfo> list) {
        if (list == null) return;
        for (AwaitHandleInfo info : list) {
            if ("4".equals(info.getType()) || "10".equals(info.getType())) {
                info.setNeedWorkers(info.getHrNeedWorkers());
                info.setConfirmedWorkers(info.getHrConfirmedWorkers());
            }
        }
    }
}
