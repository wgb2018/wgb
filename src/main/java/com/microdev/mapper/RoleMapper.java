package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Role;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMapper extends BaseMapper<Role> {
    List<Role> queryAllRolesByUserId(String userId);

    void insertRoleAndUserRelation(@Param("userId")String userId, @Param("roleId")String roleId);

    Role findByCode(String code);
}
