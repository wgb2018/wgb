package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.type.UserSex;
import com.microdev.type.UserType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@TableName("user")
public class User extends BaseEntity{
    /**
     * 是否是活跃状态
     */
    private boolean activated;
    /**
     * logo地址
     */
    private String avatar ="";
    /**
     * 生日日期
     */
    private OffsetDateTime birthday;
    /**
     * 邮箱地址
     */
    private String email ="";
    /**
     * 手机
     */
    private String mobile ="";
    /**
     * 昵称
     */
    private String nickname ="";
    /**
     * 密码
     */
    private String password ="";
    /**
     * 性别
     */
    private UserSex sex;
    /**
     * 用户类型
     */
    private UserType userType;
    /**
     * 用户名
     */
    private String username ="";
    /**
     * 小时工
     */
    private String workerId;
    /**
     * 用户编码
     */
    private String userCode ="";
	private Integer age;


    private String superior;	private int msNum;
    /**
     * 用户所属角色集合
     */
    @TableField(exist = false)
    private Set<Role> roles = new HashSet<> ();
    @TableField(exist = false)
    private String bindProtocol;
}
