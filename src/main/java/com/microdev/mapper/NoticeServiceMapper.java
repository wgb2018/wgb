package com.microdev.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeServiceMapper {
    void insert(@Param("nid") String nid, @Param("sid") String sid);
}
