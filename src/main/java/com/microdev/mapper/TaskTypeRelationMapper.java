package com.microdev.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskTypeRelationMapper {
    void insertTaskTypeRelation(@Param("id") String id,@Param("taskTypeId")String taskTypeId,@Param("idType") Integer idType);

    void deleteTaskTypeRelation(String id);
}