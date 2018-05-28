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
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.MessageQuery;
import com.microdev.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
}
