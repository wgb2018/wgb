package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.RoleMapper;
import com.microdev.model.Role;
import com.microdev.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper,Role> implements RoleService{
    @Autowired
    private RoleMapper roleMapper;
    @Override
    public List<Role> queryAllRolesByUserId(String userId) {
        return roleMapper.queryAllRolesByUserId(userId);
    }
}
