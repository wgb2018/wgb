package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.MenuModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuModelMapper extends BaseMapper<MenuModel> {

    int deleteByMenuId(@Param("menuId") String menuId);

    int insertBatch(@Param("list") List<MenuModel> list);
}
