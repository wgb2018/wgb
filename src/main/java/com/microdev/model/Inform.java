package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("inform")
public class Inform extends BaseEntity{

    /**
     * 查看状态0未读1已读
     */
    private Integer status = 0;
    /**
     * 发送类型1小时工2人力3用人单位4系统
     */
    private Integer sendType;
    /**
     * 接收类型1小时工2人力3用人单位
     */
    private Integer acceptType;
    /**
     * 通知内容
     */
    private String content;
    /**
     * 接收方id
     */
    private String receiveId;
    /**
     * 消息标题
     */
    private String title;
}
