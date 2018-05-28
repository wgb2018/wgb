package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
/**
 * @author liutf
 */
@Data
@TableName("sms_template")
public class SmsTemplate extends BaseEntity {
    /**
     * 编码： login
     */
    private String code;
    /**
     * 名称： 登录
     */
    private String name;
    /**
     * 模板内容： 您的短信验证码是{code},有效时间{minute}分钟
     */
    private String content;
    /**
     * 短信验证码有效时间,单位：分钟
     */
    private Integer lifetime;
    /**
     * 平台短信签名：微工宝
     */
    private String platformSignName;
    /**
     * 平台模板编号：SMS_126970960
     */
    private String platformTemplateCode;
    /**
     * 备注
     */
    private String remark;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLifetime() {
        return lifetime;
    }

    public void setLifetime(Integer lifetime) {
        this.lifetime = lifetime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
