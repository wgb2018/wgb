package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author liutf
 */
@Data
public class SmsTemplateDTO {
    private String id;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 短信验证码有效时间,秒为单位
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
    /**
     * 创建时间
     */
    private OffsetDateTime createTime;
    /**
     * 最后修改时间
     */
    private OffsetDateTime modifyTime;
}
