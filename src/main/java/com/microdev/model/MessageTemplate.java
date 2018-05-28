package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

/**
 * 消息通知yinbaoxin
 *
 * @author yinbaoxin
 */
@Data
@TableName("message_template")
public class MessageTemplate extends BaseEntity {

    /**
     * 消息标题
     */
    private String title;
    /**
     * 消息分类
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
