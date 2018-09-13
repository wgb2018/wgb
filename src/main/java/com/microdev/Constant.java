package com.microdev;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liutf
 */
public interface Constant {
    //时间相关
    ZoneId beijing_zoneId = ZoneId.of("Asia/Shanghai");
    ZoneOffset china_zoneOffset = ZoneOffset.of("+08:00");
    String pattern = "yyyy-MM-dd HH:mm:ss";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);

    //Redis 中的 code 的前缀
    String ACCESS_TOKEN_PREFIX = "access_token:";
    String REFRESH_TOKEN_PREFIX = "refresh_token:";

    //OSS 存储位置
    String oss_dir_avatar = "avatar/";//用户头像
    String oss_dir_idcard = "idCard/";//身份证照片
    String oss_dir_healthcard = "healthCard/";//健康证照片

    //一些不用add("/reset-pwd");add("/reset-pwd");拦截的 url
    Set<String> ignoreUrl = new HashSet<String>() {{
        //静态资源
        add("/favicon.ico");
        add("/login");//登录
        add("/register");//注册
        add("/oauth/refreshToken/*");//刷新token
        add("/resetPwd");//重置密码
        add("/smsCode/send");// 发送短信验证码
        //QQ 联合登录
        add("/auth/qq");
        add("/qqLogin/callback.do");
        //微信联合登录
        add("/openid/**");//根据 openid 获取手机号
        add("/register-weixin");//微信小程序注册
        add("/auth/weixin");
        add("/qqLogin/weixin");
        // api文档
        add("/swagger-ui.html");
        add("/webjars/**");
        add("/swagger-resources/**");
        add("/v2/api-docs");
        add("/sms-codes/**");
        add("/login-sms");
        add("/weixin/openid/*");
        add("/auth/weixin-mp");
        add("/reset-pwd");
        add("/workers/**");//应该拦截 调试中 暂不删除
        add("/**/test");
        add("/protocoldownload/*");
        add("/apply/register");
        add("/findbyname/*");
        add("/admin/dicts/*");
        add("/files");
        add("/files/UE");
        add("/check/version");
        add("/version/*");
        add("/*/guide");
        add("/downlaod");
        add("/list/evaluate");
        add("/hotels/hrcompanies/export");
        add("/hr/download/account");
        add("/hotel/download/account");
        add("/worker/download/account");
        add("/hr/worker/download");
        add("/worker/hr/download");
        add("/hr/hotel/download");
        add("/hotel/hr/download");
        add("/task/hotel/download");
        add("/hr/task/download");
        add("/worker/task/download");
        add("/worker/bill/download");
        add("/hr/bill/download");
        add("/worker/info/download");
        add("/hr/info/download");
        add("/hotel/info/download");
        add("/hotel/download/worker/pay");
        add("/hotel/pay/worker/account");
    }};


}
