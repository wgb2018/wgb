package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Holiday;
import com.microdev.param.HolidayDateInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HolidayMapper extends BaseMapper<Holiday> {

    List<Holiday> selectByTaskWorkId(String taskWorkerId);

    List<Map<String, Object>> selectUserHolidayByTaskWorkId(String taskWorkerId);

    List<HolidayDateInfo> selectHolidayByTaskWorkId(String taskWorkerId);

}