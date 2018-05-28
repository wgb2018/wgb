package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.paging.PagedList;
import com.microdev.common.paging.Paginator;
import com.microdev.model.SmsTemplate;
import com.microdev.param.SmsDTO;
import com.microdev.param.SmsTemplateDTO;
import com.microdev.param.SmsType;

import java.util.Map;

public interface SmsFacade extends IService<SmsTemplate> {
    /**
     * 发送短信通知
     */
    void sendSmsNotice(String mobile, SmsType smsType, Map<String, String> templateParams);

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String mobile, SmsType smsType);

    /**
     * 校验短信验证码
     */
    void checkSmsCode(String mobile, String smsType, String verifyCode);

    /**
     * 新增短信模板
     */
    SmsTemplateDTO addSmsTemplate(SmsTemplateDTO smsTemplate);

    /**
     * 根据短信模板 ID 查询
     */
    SmsTemplateDTO getSmsTemplateById(String id);

    /**
     * 编辑短信模板
     */
    SmsTemplateDTO editSmsTemplate(SmsTemplateDTO smsTemplate);

    /**
     * 删除短信模板
     */
    void delSmsTemplate(String id);

    /**
     * 分页查询短信模板
     */
    PagedList pagingSmsTemplate(Paginator paginator, SmsTemplateDTO smsDTO);

    /**
     * 分页查询已经发送出去的短信
     */
    PagedList<SmsDTO> pagingSms(Paginator paginator, SmsDTO smsDTO);
}
