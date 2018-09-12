package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Power;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PowerMapper extends BaseMapper<Power> {

    List<Power> selectByParam(Power power);
}
