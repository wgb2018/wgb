package com.microdev.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.PowerMenu;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PowerMenuMapper extends BaseMapper<PowerMenu> {

    int deleteByPowerId(@Param("powerId") String powerId);

    int insertBatch(@Param("list") List<PowerMenu> list);
}
