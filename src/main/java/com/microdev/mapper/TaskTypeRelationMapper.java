package com.microdev.mapper;

import com.microdev.param.AreaParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTypeRelationMapper {
    void insertTaskTypeRelation(@Param("id") String id,@Param("taskTypeId")String taskTypeId,@Param("idType") Integer idType);

    void insertTaskTypeRelationBatch(List<AreaParam> list);

    void deleteTaskTypeRelation(String id);
}