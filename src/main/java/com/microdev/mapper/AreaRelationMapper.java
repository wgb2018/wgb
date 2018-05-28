package com.microdev.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreaRelationMapper {
    void insertAreaRelation(@Param("id") String id, @Param("areaCode")String area_code, @Param("idType")Integer id_type);

    List<String> selectAreaByUserId(String id);
}
