package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Social;
import com.microdev.type.SocialType;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMapper extends BaseMapper<Social> {
    Social findByOpendIdAndType(@Param("openId")String openId, @Param("type")SocialType type);

    void save(Social social);
}
