package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Inform;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface InformMapper extends BaseMapper<Inform> {

    int insertInform(Inform inform);

    int selectUnReadCount(Map<String, Object> param);


}
