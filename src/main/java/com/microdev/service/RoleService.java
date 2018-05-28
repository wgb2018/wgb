package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.model.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> queryAllRolesByUserId(String userId);
}
