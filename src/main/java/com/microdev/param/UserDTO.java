package com.microdev.param;

import com.baomidou.mybatisplus.annotations.TableField;
import com.microdev.model.Dict;
import com.microdev.model.Model;
import com.microdev.model.Role;
import com.microdev.model.UserArea;
import com.microdev.type.PlatformType;
import com.microdev.type.SocialType;
import com.microdev.type.UserSex;
import com.microdev.type.UserType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * @author liutf
 */
@Data
public class UserDTO {
    private String tgCode;

    private String id;
    /**
     * 头像路径
     */
    private String avatar;
    /**
     * 用户名
     */
    private String username;
    /**Integerage
     * 年龄
     */
    private Integer age;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别
     */
    private UserSex sex;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 状态
     */
    private String status;
    /**
     * 密码
     */
    private String password;
    /**
     * 设备ID
     */
    private String deviceId;
    /**
     * 短信验证码
     */
    private String smsCode;
    /**
     * 用户类型
     */
    private UserType userType;
    /**
     * 用户二维码
     */
    private String qrCode;

    private String uniqueId = "";

    public Integer getUserCode() {
        return userCode;
    }

    public void setUserCode(Integer userCode) {
        this.userCode = userCode;
    }

    /**
     * 用户类型编码
     */
    private Integer userCode;
    /**
     * 是否已经激活
     */
    private Boolean activated;
    /**
     * 平台
     */
    private PlatformType platform;
    /**
     * 创建时间
     */
    private OffsetDateTime createTime;
    /**
     * 最后修改时间
     */
    private OffsetDateTime modifyTime;

    /**
     * 联合登录类型
     */
    private SocialType socialType;
    /**
     * 联合登录 openId
     */
    private String openId;

    private Set<PermissionDTO> permissions = new HashSet<>();
    private List<Role> roleList = new ArrayList<>();
	/**
     * 服务类型
     */
    private List<Dict> serviceType;
    /**
     * 服务地区
     */
    private List<UserArea> areaCode;
    /**
     * 所属公司
     */
    private CompanyDTO company;

    private String workerId;

    private String idCardNumber;

    private String healthCard;

    private String idCardFront;

    private String idCardBack;

	private OffsetDateTime birthday;

	private String birthdayNew;

    private String handheldIdentity;

    private Integer stature;

    private Integer weight;

    private Integer education;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserSex getSex() {
        return sex;
    }

    public void setSex(UserSex sex) {
        this.sex = sex;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformType platform) {
        this.platform = platform;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public OffsetDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(OffsetDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public SocialType getSocialType() {
        return socialType;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public Set<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getHealthCard() {
        return healthCard;
    }

    public void setHealthCard(String healthCard) {
        this.healthCard = healthCard;
    }

    public String getIdCardFront() {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack() {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack) {
        this.idCardBack = idCardBack;
    }
}
