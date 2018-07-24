package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Evaluate;
import com.microdev.param.EvaluateParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EvaluateMapper extends BaseMapper<Evaluate> {
    List<Evaluate> queryList(EvaluateParam request);
    void deleteAll(@Param ("type") Integer type,@Param ("level") Integer level);

    List<String> selectLabelsInfo(@Param("commentId") String commentId);
}
