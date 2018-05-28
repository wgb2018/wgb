package com.microdev.converter;

import com.microdev.model.Sms;
import com.microdev.model.SmsTemplate;
import com.microdev.param.SmsDTO;
import com.microdev.param.SmsTemplateDTO;
import org.springframework.stereotype.Component;

/**
 * @author liutf
 */
@Component
public class SmsConverter {

    public SmsDTO toDTO(Sms sms) {
        if (sms == null) return null;

        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setId(sms.getPid());
        smsDTO.setSmsType(sms.getSmsType());
        smsDTO.setMobile(sms.getMobile());
        smsDTO.setContent(sms.getContent());
        smsDTO.setDateCreated(sms.getCreateTime());
        smsDTO.setDateUpdated(sms.getModifyTime());
        smsDTO.setPlatformTemplateCode(sms.getPlatformTemplateCode());
        smsDTO.setPlatformSignName(sms.getPlatformSignName());
        return smsDTO;
    }

    public SmsTemplate toDO(SmsTemplateDTO templateDTO) {
        if (templateDTO == null) return null;

        SmsTemplate template = new SmsTemplate();
        template.setPid(templateDTO.getId());
        template.setCode(templateDTO.getCode());
        template.setName(templateDTO.getName());
        template.setContent(templateDTO.getContent());
        template.setLifetime(templateDTO.getLifetime());
        template.setPlatformTemplateCode(templateDTO.getPlatformTemplateCode());
        template.setPlatformSignName(templateDTO.getPlatformSignName());
        template.setRemark(templateDTO.getRemark());
        return template;
    }

    public SmsTemplateDTO toDTO(SmsTemplate template) {
        if (template == null) return null;

        SmsTemplateDTO smsTemplateDTO = new SmsTemplateDTO();
        smsTemplateDTO.setId(template.getPid());
        smsTemplateDTO.setCode(template.getCode());
        smsTemplateDTO.setName(template.getName());
        smsTemplateDTO.setContent(template.getContent());
        smsTemplateDTO.setLifetime(template.getLifetime());
        smsTemplateDTO.setCreateTime(template.getCreateTime());
        smsTemplateDTO.setModifyTime(template.getModifyTime());
        smsTemplateDTO.setPlatformSignName(template.getPlatformSignName());
        smsTemplateDTO.setPlatformTemplateCode(template.getPlatformTemplateCode());
        smsTemplateDTO.setRemark(template.getRemark());
        return smsTemplateDTO;
    }
}
