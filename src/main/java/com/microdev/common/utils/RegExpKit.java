package com.microdev.common.utils;

import java.util.regex.Pattern;


/**
 * @author liutf
 */
public class RegExpKit {
    /**
     * 校验手机号
     *
     * @return 校验通过返回true，否则返回false
     * @Param mobile
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    /**
     * 校验邮箱
     *
     * @return 校验通过返回true，否则返回false
     * @Param email
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(REGEX_EMAIL, email);
    }

    /**
     * 校验密码强度
     *
     * @return 校验通过返回true，否则返回false
     * @Param 密码
     */
    public static boolean isPwd(String password) {
        return Pattern.matches(REGEX_PWD, password);
    }

    /**
     * 校验用户名
     *
     * @param username
     * @return
     */
    public static boolean isUsername(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
    }

    /**
     * 正则表达式：验证手机号码
     */
    public static final String REGEX_MOBILE = "^1\\d{10}$";
    /**
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    /**
     * 密码: 6-18位字母、数字、下划线
     */
    public static final String REGEX_PWD = "^[a-zA-Z0-9_]{6,18}\\$";
    /**
     * 用户名：6-18位，字母开头，数字、下划线混合
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,18}\\$";
}
