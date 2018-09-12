package com.microdev.param;

import lombok.Data;

@Data
public class AgentAccountRegister {

    //权限角色
    private String roleId;
    //账号名称
    private String name;
    //密码
    private String password;
    //代理商编号
    private String identifer;

    private String id;
}
