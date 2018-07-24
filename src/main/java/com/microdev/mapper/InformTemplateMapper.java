package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.InformTemplate;
import com.microdev.param.InformType;
import org.springframework.stereotype.Repository;

@Repository
public interface InformTemplateMapper extends BaseMapper<InformTemplate> {
    InformTemplate selectByCode(String code);
}
