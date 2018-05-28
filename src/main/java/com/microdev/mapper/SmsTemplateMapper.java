package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.SmsTemplate;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsTemplateMapper extends BaseMapper<SmsTemplate> {
    SmsTemplate findByCode(String code);

    SmsTemplate findById(String id);

    void save(SmsTemplate smsTemplate);

    void update(SmsTemplate smsTemplate);

}
