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
import com.microdev.service.InformService;
import com.microdev.service.MessageService;
import com.microdev.service.TaskHrCompanyService;
import com.microdev.service.TaskService;
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
     * 设置消息已读
     */
    @Override
    public ResultDO updateMsgStatus(String id) {
        messageMapper.updateStatus(id);
        return ResultDO.buildSuccess("消息已读");
    }
    /**
     * 酒店绑定或解绑人力公司 人力解绑或绑定酒店
     */
    @Override
    public boolean hotelBindHrCompany(Set<String> bindCompany, Company applyCompany, String pattern, Integer type) {
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
        return true;
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
            m.setHrCompanyId (dto.getHrCompanyId ());
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
    public void hrDistributeTask(List<Map<String, String>> list, String hrId, String hrName, String pattern, String taskId, String hrTaskId) {
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
            System.out.println("workerTaskId:" + param.get("workerTaskId"));
            m.setTaskId(taskId);
            m.setIsTask(0);
            m.setHotelId(param.get("hotelId"));
            m.setHrTaskId(hrTaskId);

            m.setMessageContent(c);
            messageList.add(m);
        }
        messageMapper.saveBatch(messageList);
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
     * 查询未读消息
     * @param id            角色id
     * @param applyType     角色类型小时工worker人力hr酒店hotel
     * @return
     */
    @Override
    public List<Message> selectUnReadMessage(String id, String applyType) {

        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(applyType)) {
            throw new ParamsException("参数不能为空");
        }
        /*Map<String, Object> param = new HashMap<>();
        param.put("applyType", applyType);
        if ("worker".equals(applyType)) {
            param.put("workerId", id);
        } else if ("hr".equals(applyType)) {
            param.put("hrCompanyId", id);
        } else if ("hotel".equals(applyType)) {
            param.put("hotelId", id);
        } else {
            throw new ParamsException("参数applyType类型错误");
        }
        List<Message> list = messageMapper.selectUnReadMessage(param);
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }*/
        return null;
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
        Map<String, Integer> param = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        if ("worker".equals(applyType)) {
            //查询当前任务未读数量
            param.put("curTask", taskWorkerMapper.selectWorkerUnreadCount(id));
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
            //查询已完成事物未读数量
            param.put("completeTask", taskWorkerMapper.selectCompleteCount(id));
            //查询补签数量
            List<Integer> list = workerLogMapper.selectUnreadPunchCount();
            if (list == null) {
                param.put("supplement", 0);
            } else {
                param.put("supplement", list.size());
            }

        } else if ("hr".equals(applyType)) {

            param.put("curTask", taskHrCompanyService.selectUnreadCount(id));
            map.put("hrId", id);
            map.put("roleName", "hr");
            param.put("pendingTask", messageMapper.selectUnReadMessage(map));
            map = new HashMap<>();
            map.put("status", 0);
            map.put("acceptType", 2);
            map.put("receiveId", id);
            param.put("notice", informService.selectCountByParam(map));
            //查询已完成事物未读数量
            param.put("completeTask", taskService.selectCompleteAmount(id));
        } else if ("hotel".equals(applyType)) {

            param.put("curTask", taskService.selectUnReadAmount(id));
            map.put("hotelId", id);
            map.put("roleName", "hotel");
            param.put("pendingTask", messageMapper.selectUnReadMessage(map));
            map = new HashMap<>();
            map.put("status", 0);
            map.put("acceptType", 3);
            map.put("receiveId", id);
            param.put("notice", informService.selectCountByParam(map));
            //查询已完成事物未读数量
            param.put("completeTask", taskHrCompanyService.selectCompleteCount(id));
        } else {
            throw new ParamsException("参数applyType类型错误");
        }

        return param;
    }

    /**
     * 查询未读消息数量
     * @param id
     * @param applyType
     * @param type  type=1时
     * @return
     */
    @Override
    public int selectMessageCount(String id, String applyType, int type) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(applyType)) {
            throw new ParamsException("参数不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("applyType", applyType);
        if ("1".equals(applyType)) {
            param.put("workerId", id);
        } else if ("2".equals(applyType)) {
            param.put("hrCompanyId", id);
        } else if ("3".equals(applyType)) {
            param.put("hotelId", id);
        } else {
            throw new ParamsException("参数applyType类型错误");
        }
        param.put("checkSign", type);

        return messageMapper.selectUnReadCount(param);
    }

    /**
     * 查询未处理的消息数量
     * @param id
     * @param applyType
     * @param status
     * @return
     */
    @Override
    public int selectUnHandleMessageAmount(String id, String applyType, int status) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(applyType)) {
            throw new ParamsException("参数不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        if ("1".equals(applyType)) {
            param.put("workerId", id);
        } else if ("2".equals(applyType)) {
            param.put("hrCompanyId", id);
        } else if ("3".equals(applyType)) {
            param.put("hotelId", id);
        } else {
            throw new ParamsException("参数applyType类型错误");
        }
        param.put("applyType", applyType);
        param.put("status", 0);
        param.put("checkSign", 0);

        return messageMapper.selectUnReadCount(param);
    }

    /**
     * 查询用户的消息
     * @param id           用户id
     * @param role         角色类型 1小时工2人力公司3酒店
     * @param type         显示消息类型1小时工2人力公司3酒店4系统
     * @param page         页码
     * @param pageNum      页数
     * @return
     */
    @Override
    public MessageShowDTO selectMessage(String id, int role, int type, int page, int pageNum) {
        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数不能为空");
        }
        MessageShowDTO message = new MessageShowDTO();
        Map<String, Object> param = new HashMap<>();
        if (role == 1) {
            //小时工消息
            param.put("workerId", id);
            param.put("applicantType", 3);
            param.put("applyType", 1);
            param.put("status", 0);
            message.setCompanyNum(messageMapper.selectUnReadCount(param));
            param.put("applicantType", 2);
            message.setHrNum(messageMapper.selectUnReadCount(param));
            param.put("applicantType", 4);
            message.setSystemNum(messageMapper.selectUnReadCount(param));
            PageHelper.startPage(page, pageNum, true);
            if (type == 2) {
                param.put("applicantType", 2);
                message.setHrList(messageMapper.selectByParam(param));
            } else if (type == 3) {
                param.put("applicantType", 3);
                message.setCompanyList(messageMapper.selectByParam(param));
            } else if (type == 4) {
                param.put("applicantType", 4);
                message.setSystemList(messageMapper.selectByParam(param));
            } else {
                throw new ParamsException("参数值错误");
            }

        } else if (role == 2) {
            param.put("hrCompanyId", id);
            param.put("applicantType", 1);
            param.put("applyType", 2);
            message.setWorkerNum(messageMapper.selectUnReadCount(param));
            param.put("applicantType", 3);
            message.setCompanyNum(messageMapper.selectUnReadCount(param));
            param.put("applicantType", 4);
            message.setSystemNum(messageMapper.selectUnReadCount(param));
            PageHelper.startPage(page, pageNum, true);
            if (type == 1) {
                param.put("applicantType", 1);
                message.setWorkerList(messageMapper.selectByParam(param));
            } else if (type == 3) {
                param.put("applicantType", 3);
                message.setCompanyList(messageMapper.selectByParam(param));
            } else if (type == 4) {
                param.put("applicantType", 4);
                message.setSystemList(messageMapper.selectByParam(param));
            } else {
                throw new ParamsException("参数值错误");
            }

        } else if (role == 3) {
            param.put("hrCompanyId", id);
            param.put("applicantType", 2);
            param.put("applyType", 3);
            message.setHrNum(messageMapper.selectUnReadCount(param));
            param.put("applicantType", 4);
            message.setSystemNum(messageMapper.selectUnReadCount(param));
            if (type == 2) {
                param.put("applicantType", 2);
                message.setHrList(messageMapper.selectByParam(param));
            } else if (type == 4) {
                param.put("applicantType", 4);
                message.setSystemList(messageMapper.selectByParam(param));
            } else {
                throw new ParamsException("参数错误");
            }
        } else {
            throw new ParamsException("参数值错误");
        }
        return message;
    }

    /**
     * 更新消息
     * @param id
     * @return
     */
    @Override
    public String updateMessageCheckSign(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数错误");
        }
        messageMapper.updateMessageCheckSign(id);
        return "成功";
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
            list = messageMapper.selectHotelAwaitHandleInfo(request.getId());
        } else if ("hr".equals(request.getType())) {
            list = messageMapper.selectHrAwaitHandleInfo(request.getId());
        } else {
            throw new ParamsException("参数传递错误");
        }
        System.out.println ("list:"+list);
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
        if ("12".equals(messagetype)) {
            response = messageMapper.selectWorkerApply(messageId);
            response.setOriginator(response.getName());
            if (response.getAge() < 0) response.setAge(0);
        } else if ("13".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectCompanyApply(messageId);
                response.setOriginator(response.getCompanyName());
            } else if ("hotel".equals(messagetype)) {
                response = messageMapper.hotelHrApplyCooperate(messageId);
                response.setOriginator(response.getCompanyName());
            } else {
                throw new ParamsException("用户类型错误");
            }
        } else if ("5".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectWorkerApply(messageId);
                if (response.getAge() < 0) response.setAge(0);
                response.setOriginator(response.getName());
            } else if ("worker".equals(messagetype)) {
                response = messageMapper.hotelHrApplyCooperate(messageId);
                response.setOriginator(response.getCompanyName());
            } else {
                throw new ParamsException("用户类型错误");
            }
        } else if ("8".equals(type)) {
            if ("worker".equals(messagetype)) {
                response = messageMapper.selectPayConfirm(messageId, "2");

            } else if ("hr".equals(messagetype)){
                response = messageMapper.selectPayConfirm(messageId, "1");
            }
            response.setOriginator(response.getCompanyName());

        } else if ("1".equals(type) ) {
            response = messageMapper.selectSupplementApply(messageId);
            if (response.getAge() < 0) response.setAge(0);
            response.setOriginator(response.getName());
        } else if ("2".equals(type)){
            response = messageMapper.selectOvertimeApply(messageId);
            if (response.getAge() < 0) response.setAge(0);
            response.setOriginator(response.getName());
        } else if ("3".equals(type)) {
            response = messageMapper.selectLeaveApply(messageId);
            if (response.getAge() < 0) response.setAge(0);
            response.setOriginator(response.getName());
        } else if ("4".equals(type)) {
            response = messageMapper.selectApplyAllocate(messageId);
            response.setOriginator(response.getCompanyName());
        } else if ("7".equals(type)) {
            response = messageMapper.selectWorkerApply(messageId);
            if (response.getAge() < 0) response.setAge(0);
            response.setOriginator(response.getName());
        } else if ("9".equals(type)) {
            response = messageMapper.selectWorkerApply(messageId);
            if (response.getAge() < 0) response.setAge(0);
            String companyName = messageMapper.selectCompanyNameByMessageId(messageId);
            response.setOriginator(companyName);
        } else if ("10".equals(type)) {
            if ("hr".equals(messagetype)) {
                response = messageMapper.selectWorkerApply(messageId);
                if (response.getAge() < 0) response.setAge(0);
                response.setOriginator(response.getName());
            } else if ("hotel".equals(messagetype)) {
                response = messageMapper.selectHotelApply(messageId);
                response.setOriginator(response.getCompanyName());
            } else {
                throw new ParamsException("用户类型错误");
            }
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
            if ("hr".equals(type)) {
                response = messageMapper.selectHrAwaitHandleTask(messageId);
            } else if ("hotel".equals(type)) {
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
        if ("0".equals(response.getHourlyPay())) {
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
    public void hrDistributeWorkerTask(List<TaskWorker> list, TaskHrCompany taskHrCompany) {
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
            message.setMessageContent(str);
            message.setHrTaskId(taskHrCompany.getPid());
            message.setApplyType(0);
            message.setApplicantType(2);
            message.setHotelId(taskHrCompany.getHotelId());
            message.setHrCompanyId(taskHrCompany.getHrCompanyId());
            message.setIsTask(0);
            message.setTaskId (taskHrCompany.getTaskId ());
            messageMapper.insert(message);
        }

    }

    /**
     * 发送消息
     * @param param
     */
    @Override
    public void sendMessageInfo(Map<String, Object> param) {
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
        messageMapper.insert(message);
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

}
