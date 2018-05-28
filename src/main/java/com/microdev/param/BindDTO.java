package com.microdev.param;

import com.microdev.type.PlatformType;
import com.microdev.type.SocialType;
import com.microdev.type.UserType;
import lombok.Data;

/**
 * @author liutf
 */
@Data
public class BindDTO {
    /**
     * 登录平台
     */
    private PlatformType platform;
    /**
     * 联合登录类型
     */
    private SocialType socialType;
    /**
     * 用户类型
     */
    private UserType userType;
    /**
     * openId
     */
    private String openId;
    /**
     * 短信验证码
     */
    private String smsCode;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 密码
     */
    private String password;
}
