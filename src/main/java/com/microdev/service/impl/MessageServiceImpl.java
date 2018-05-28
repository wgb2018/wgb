package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.mapper.MessageMapper;
import com.microdev.mapper.MessageTemplateMapper;
import com.microdev.model.Company;
import com.microdev.model.Message;
import com.microdev.model.MessageTemplate;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.MessageQuery;
import com.microdev.param.TaskHrCompanyDTO;
import com.microdev.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Transactional
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper,Message> implements MessageService{
    @Autowired
    MessageTemplateMapper messageTemplateMapper;
    @Autowired
    MessageMapper messageMapper;
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
        if (bindCompany == null || bindCompany.size() == 0 || applyCompany == null) {
            throw new ParamsException("参数不能为空");
        }
        if (type != 1 && type != 2) {
            throw new ParamsException("参数type错误");
        }

        List<Message> list = new ArrayList<>();
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        Iterator<String> it = bindCompany.iterator();
        Map<String, String> param = null;
        Message m = null;
        String id = null;
        while (it.hasNext()) {
            id = it.next();
            m = new Message();
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            param = new HashMap<>();
            if (type == 1) {
                m.setApplyType(2);
                m.setHotelId(applyCompany.getPid());
                m.setHrCompanyId(id);
            } else {
                m.setApplyType(3);
                m.setHotelId(id);
                m.setHrCompanyId(applyCompany.getPid());
            }
            param.put("userName", applyCompany.getName());
            String content = StringKit.templateReplace(mess.getContent(), param);
            m.setMessageContent(content);
            list.add(m);
        }
        messageMapper.saveBatch(list);
        return true;
    }

    /**
     * 小时工绑定或解绑人力公司
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
        Iterator<String> it = hrCompanyId.iterator();
        Message m = null;
        List<Message> list = new ArrayList<>();
        while (it.hasNext()) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            m.setWorkerId(workerId);
            Map<String, String> param = new HashMap<>();
            param.put("userName", userName);
            String c = StringKit.templateReplace(mess.getContent(), param);
            m.setMessageContent(c);
            m.setHrCompanyId(it.next());
            m.setApplyType(2);
            list.add(m);
        }
        messageMapper.saveBatch(list);
    }

    /**
     * 酒店向人力公司派发任务
     * @param request
     * @param hotel
     * @param pattern
     * @return
     */
    @Override
    public void hotelDistributeTask(CreateTaskRequest request, Company hotel, String pattern) {
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
        for (TaskHrCompanyDTO dto : request.getHrCompanySet()) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            m.setApplyType(2);
            m.setHrCompanyId(dto.getHrCompanyId());
            m.setHotelId(hotel.getPid());
            Map<String, String> param = new HashMap<>();
            param.put("hotelName", hotel.getName());
            param.put("taskContent", request.getTaskContent());

            String c = StringKit.templateReplace(mess.getContent(), param);
            m.setMessageContent(c);
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
     */
    @Override
    public void hrDistributeTask(List<Map<String, String>> list, String hrId, String hrName, String pattern) {
        if (list == null || list.size() == 0 || StringUtils.isEmpty(hrId) || StringUtils.isEmpty(pattern)) {
            throw new ParamsException("参数不能为空");
        }
        MessageTemplate mess = messageTemplateMapper.findFirstByCode(pattern);
        if (mess == null) {
            throw new ParamsException("模板查询不到");
        }
        Message m = null;
        List<Message> messageList = new ArrayList<>();
        for (Map<String, String> param : list) {
            m = new Message();
            m.setDeleted(false);
            m.setMessageCode(mess.getCode());
            m.setMessageTitle(mess.getTitle());
            m.setStatus(0);
            m.setApplyType(0);
            m.setHrCompanyId(hrId);
            m.setWorkerId(param.get("workerId"));
            m.setWorkerTaskId(param.get("workerTaskId"));
            Map<String, String> map = new HashMap<>();
            map.put("hrCompanyName", hrName);
            String c = StringKit.templateReplace(mess.getContent(), map);
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
        m.setMessageTitle(mess.getTitle());
        m.setStatus(0);
        m.setApplyType(0);
        m.setHrCompanyId(c.getHrCompanyId());
        m.setHotelId(c.getHotelId());
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
     * @param id
     * @param applyType
     * @return
     */
    @Override
    public List<Message> selectUnReadMessage(String id, String applyType) {

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
        List<Message> list = messageMapper.selectUnReadMessage(param);
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        return list;
    }

    /**
     * 查询未读消息数量及各个类型的数量
     * @param id
     * @param applyType
     * @return
     */
    @Override
    public Map<String, Integer> selectUnReadCount(String id, String applyType) {
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
        int total = messageMapper.selectUnReadCount(param);
        Map<String, Integer> result = new HashMap<>();
        result.put("total", total);
        //查询补签申请数量
        param.put("messageCode", "applySupplementMessage");
        int supplementCount = messageMapper.selectUnReadCount(param);
        result.put("supplementCount", supplementCount);
        //查询加时申请
        param.put("messageCode", "applyExtraMessage");
        int extraCount = messageMapper.selectUnReadCount(param);
        result.put("extraCount", extraCount);
        //请假申请
        param.put("messageCode", "applyLeaveMessage");
        int leaveCount = messageMapper.selectUnReadCount(param);
        result.put("leaveCount", leaveCount);
        //调配申请
        param.put("messageCode", "applyChangeMessage");
        int changeCount = messageMapper.selectUnReadCount(param);
        result.put("changeCount", changeCount);
        //绑定申请
        param.put("messageCode", "applyBindMessage");
        int bindCount = messageMapper.selectUnReadCount(param);
        result.put("bindCount", bindCount);

        return result;
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
        param.put("status", status);
        return messageMapper.selectUnReadCount(param);
    }

}
