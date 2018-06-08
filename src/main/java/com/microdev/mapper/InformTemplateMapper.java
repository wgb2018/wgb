package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.InformTemplate;
import com.microdev.param.InformType;

public interface InformTemplateMapper extends BaseMapper<InformTemplate> {
    InformTemplate selectByCode(InformType code);
}
