package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.ServiceComment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCommentMapper extends BaseMapper<ServiceComment> {

    int saveInfo(ServiceComment serviceComment);
}
