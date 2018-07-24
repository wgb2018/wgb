package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.EvaluteGrade;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluteGradeMapper extends BaseMapper<EvaluteGrade> {

    int saveInfo(EvaluteGrade grade);

    EvaluteGrade selectByRoleId(String roleId);
}
