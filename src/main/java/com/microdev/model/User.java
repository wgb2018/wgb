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

	private String qrCode;
    /**
     * 用户所属角色集合
     */
    @TableField(exist = false)
    private Set<Role> roles = new HashSet<> ();

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public OffsetDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(OffsetDateTime birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserSex getSex() {
        return sex;
    }

    public void setSex(UserSex sex) {
        this.sex = sex;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
}
