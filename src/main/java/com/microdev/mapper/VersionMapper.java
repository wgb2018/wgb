package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Version;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionMapper extends BaseMapper <Version> {
    @Select("select version_code from version order by create_time desc  limit 1")
    String selectVersion();
}
