package com.microdev.param;

import lombok.Data;

import java.util.Map;

@Data
public class SmsSendDTO {
    /**
     * 待发送手机号
     */
    private String mobile;
    /**
     * 短信签名-可在短信控制台中找到
     */
    private String signName;
    /**
     * 短信模板-可在短信控制台中找到
     */
    private String templateCode;
    /**
     * 模板中的变量替换，如:亲爱的${name},您的验证码为${code}此处的值为
     * name=tom
     * code=123
     */
    private Map<String, String> templateParam;
}