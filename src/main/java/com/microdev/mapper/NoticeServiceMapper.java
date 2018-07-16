package com.microdev.mapper;

import com.microdev.model.Dict;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeServiceMapper {
    void insert(@Param("nid") String nid, @Param("sid") String sid);
    List<Dict> queryService(String id);
}
