package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.exception.ParamsException;

import com.microdev.common.paging.PagedList;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.SmsConverter;
import com.microdev.mapper.SmsMapper;
import com.microdev.mapper.SmsTemplateMapper;
import com.microdev.model.Sms;
import com.microdev.model.SmsTemplate;
import com.microdev.param.*;
import com.microdev.service.SmsFacade;
import com.microdev.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Transactional
@Service
public class SmsFacadeImpl extends ServiceImpl<SmsTemplateMapper,SmsTemplate> implements SmsFacade {
    private static final String SMS_CODE_PREFIX = "sms-code:";
    private static final String SMS_CODE_SPLIT = "|";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    SmsTemplateMapper smsTemplateMapper;
    @Autowired
    private ContactProperties contactProperties;
    @Autowired
    SmsService smsService;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private SmsConverter smsConverter;



    /**
     * 发送短信验证码
     */
    @Override
    public void sendSmsCode(String mobile, SmsType smsType) {
        SmsTemplate smsTemplate = smsTemplateMapper.findByCode(smsType.name());
        if (smsTemplate == null) {
            throw new ParamsException("无法找到" + smsType + "短信模板");
        }

        Integer lifetime = smsTemplate.getLifetime();
        String tplStr = smsTemplate.getContent();

        //生成短信验证码
        String code = StringKit.numberStrGenerator(contactProperties.getSms().getSmsCodeLength());
        //替换短信模板内容
        LinkedHashMap<String, String> templateParams = new LinkedHashMap<>(2);
        templateParams.put("code", code);
        templateParams.put("minute", lifetime.toString());
        String content = StringKit.templateReplace(tplStr, templateParams);


        if (smsTemplate.getPlatformTemplateCode() == null) {
            //调用短信通道发送短信
            //保存短信发送记录
            //保存验证码到Redis
        } else {
            //调用短信通道发送短信
            SmsSendDTO smsSendDTO = new SmsSendDTO();
            smsSendDTO.setMobile(mobile);
            smsSendDTO.setSignName(smsTemplate.getPlatformSignName());
            smsSendDTO.setTemplateCode(smsTemplate.getPlatformTemplateCode());
            smsSendDTO.setTemplateParam(templateParams);
            smsService.sendSms(smsSendDTO);

            //保存短信发送记录
            Sms sms = new Sms();
            sms.setSmsType(smsType);
            sms.setMobile(mobile);
            sms.setContent(content);
            sms.setPlatformSignName(smsTemplate.getPlatformSignName());
            sms.setPlatformTemplateCode(smsTemplate.getPlatformTemplateCode());
            sms.setDeleted(false);
            smsMapper.insert(sms);
            //保存验证码到Redis
            String key = SMS_CODE_PREFIX + mobile + SMS_CODE_SPLIT + smsType;
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, code, lifetime, TimeUnit.MINUTES);
        }
    }

    /**
     * 发送短信通知
     * hr_dispatch_worker   人力公司派单给小时工
     * worker_feedback_hr   小时工反馈任务状态给人力公司
     */
    @Override
    public void sendSmsNotice(String mobile, SmsType smsType, Map<String, String> templateParams) {
        SmsTemplate smsTemplate = smsTemplateMapper.findByCode(smsType.name());
        if (smsTemplate == null) {
            throw new ParamsException("无法找到" + smsType + "短信模板");
        }
        String content = StringKit.templateReplace(smsTemplate.getContent(), templateParams);
        //调用短信通道发送短信
        SmsSendDTO smsSendDTO = new SmsSendDTO();

        smsSendDTO.setMobile(mobile);
        smsSendDTO.setSignName(smsTemplate.getPlatformSignName());
        smsSendDTO.setTemplateCode(smsTemplate.getPlatformTemplateCode());
        smsSendDTO.setTemplateParam(templateParams);
        smsService.sendSms(smsSendDTO);

        //保存短信发送记录
        Sms sms = new Sms();
        sms.setSmsType(smsType);
        sms.setMobile(mobile);
        sms.setContent(content);
        sms.setPlatformSignName(smsTemplate.getPlatformSignName());
        sms.setPlatformTemplateCode(smsTemplate.getPlatformTemplateCode());
        smsMapper.insert(sms);
    }
    /**
     * 校验短信验证码
     */
    @Override
    public void checkSmsCode(final String mobile, final String smsType, String verifyCode) {
        String key = SMS_CODE_PREFIX + mobile + SMS_CODE_SPLIT + smsType;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String smsCode = operations.get(key);
        System.out.println (smsCode+"---"+verifyCode);
        if (smsCode == null || !smsCode.equals(verifyCode)) {
            throw new ParamsException("短信验证码不正确");
        }

        redisTemplate.delete(key);
    }

    /**
     * 新增短信模板
     */
    @Override
    public SmsTemplateDTO addSmsTemplate(SmsTemplateDTO smsTemplateDTO) {
        SmsTemplate smsTemplate = smsConverter.toDO(smsTemplateDTO);
        smsTemplateMapper.insert(smsTemplate);
        return smsConverter.toDTO(smsTemplate);
    }

    /**
     * 编辑短信模板
     */
    @Override
    public SmsTemplateDTO editSmsTemplate(SmsTemplateDTO smsTemplateDTO) {
        SmsTemplate smsTemplate = smsConverter.toDO(smsTemplateDTO);
        smsTemplateMapper.updateById(smsTemplate);
        return smsConverter.toDTO(smsTemplate);
    }

    /**
     * 删除短信模板
     */
    @Override
    public void delSmsTemplate(String id) {
        SmsTemplate smsTemplate =  smsTemplateMapper.findById(id);
        smsTemplate.setDeleted(true);
        smsTemplateMapper.updateById(smsTemplate);
    }

    /**
     * 根据短信模板 ID 查询
     */
    @Transactional(readOnly = true)
    @Override
    public SmsTemplateDTO getSmsTemplateById(String id) {
        SmsTemplate smsTemplate =  smsTemplateMapper.findById(id);
        return smsConverter.toDTO(smsTemplate);
    }

    /**
     * 分页查询短信模板
     */
    @Transactional(readOnly = true)
    @Override
    public PagedList<SmsTemplateDTO> pagingSmsTemplate(Paginator paginator, SmsTemplateDTO smsTemplateDTO) {
        return null;
    }


    /**
     * 分页查询已经发送出去的短信
     */
    @Transactional(readOnly = true)
    @Override
    public PagedList<SmsDTO> pagingSms(Paginator paginator, final SmsDTO sms) {
        return null;
    }


}
