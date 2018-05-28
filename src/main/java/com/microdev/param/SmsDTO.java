package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author liutf
 */
@Data
public class SmsDTO {
    private String id;
    /**
     * 短信签名
     */
    private String platformSignName;
    /**
     * 短信模板编号
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
    /**
     * 创建时间
     */
    private OffsetDateTime dateCreated;
    /**
     * 最后修改时间
     */
    private OffsetDateTime dateUpdated;
    /**
     * 开始时间，用于承载前端传入查询参数
     */
    private OffsetDateTime dateStart;
    /**
     * 结束时间，用于承载前端传入查询参数
     */
    private OffsetDateTime dateEnd;
}
