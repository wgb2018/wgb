package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.MessageTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageTemplateMapper extends BaseMapper<MessageTemplate> {
    MessageTemplate findFirstByCode(String code);

    void save(MessageTemplate messageTemplate);

    void update(MessageTemplate messageTemplate);

    List<MessageTemplate> findAll();
}
