package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Dict;
import com.microdev.model.UserArea;
import com.microdev.param.DictDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DictMapper extends BaseMapper<Dict> {
    DictDTO findByNameAndCode(@Param("name")String name, @Param("code")String code);

    void save(Dict dict);

    Dict findOne(String id);

    void delete(String id);

    void update(Dict dict);

    List<Dict> findByName(String name);

    List<Dict> findAll();

    List<Dict> queryDicts(DictDTO dictDTO);

    List<Map<String,String>> findProvince();

    String findProvinceNameById(String id);

    String findCityNameById(String id);

    String findAreaNameById(String id);

    List<Map<String,String>> findCity(String pid);

    List<Map<String,String>> findArea(String cid);

    List<String> selectTypeByUserId(String id);

    List<Dict> queryTypeByUserId(String id);

    List<UserArea> findServiceArea(String id);

    UserArea findSeriveAreaSecond(String id);

    UserArea findSeriveAreaFirst(String id);

    UserArea findSeriveAreaThird(String id);

    Integer isProvince(String id);
    Integer isCity(String id);
    Integer isArea(String id);
    List<String> selectMaxCode();


}
