package com.microdev.param;

import lombok.Data;

@Data
public class CreateMsgTemplateRequest {

    /**
     * 消息标题
     */
    private String title;
    /**
     * 消息编码
     */
    private String code;
    /**
     * 消息内容模板
     */
    private String content;
    /**
     * 消息跳转页面路径
     */
    private String msgLink;
}
