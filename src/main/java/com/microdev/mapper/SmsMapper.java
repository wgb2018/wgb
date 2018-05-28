package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Sms;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsMapper extends BaseMapper<Sms> {
    void save(Sms sms);
}
