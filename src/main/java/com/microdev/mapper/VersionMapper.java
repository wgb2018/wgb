package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Version;
import com.microdev.param.VersionRequest;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionMapper extends BaseMapper <Version> {
    @Select("select * from version")
    List<Version> selectVersion();

    List<Version> queryVersions(VersionRequest versionRequest);

    Version version(String type);

}
