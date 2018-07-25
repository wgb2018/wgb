package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Holiday;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HolidayMapper extends BaseMapper<Holiday> {

    List<Holiday> selectByTaskWorkId(String taskWorkerId);

}