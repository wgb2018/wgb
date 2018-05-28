package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.param.SmsType;
import lombok.Data;


/**
 * @author liutf
 */
@Data
@TableName("sms")
public class Sms extends BaseEntity {
    /**
     * 平台短信签名：微工宝
     */
    private String platformSignName;
    /**
     * 平台模板编号：SMS_126970960
     */
    private String platformTemplateCode;
    /**
     * 短信类型
     */
    private SmsType smsType;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 短信内容
     */
    private String content;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
