package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;


/**
 * 消息通知yinbaoxin
 *
 * @author yinbaoxin
 */
@Data
@TableName("message")
public class Message extends BaseEntity {

    /**
     * 消息标题
     */
    private String messageTitle;

    /**
     * 消息内容
     */
    private String messageContent;
    /**
     * 消息分类
     */
    private String messageCode;
    /**
     * 消息链接页面
     */
    private String messageLink;
    /**
     * 小时工
     */
    private String workerId;
    /**
     * 小时工任务id
     */
    private String workerTaskId;
    /**
     * 酒店id
     */
    private String hotelId;
    /**
     * 人力资源公司id
     */
    private String hrCompanyId;
    /**
     *消息发送者类型1小时工2人力公司3酒店4系统
     */
    private int applicantType;
    /**
     * 申请类型0代表人力公司派发任务给小时工，1代表被申请方为小时工，2代表被申请方为人力资源公司，      *  3代表被申请方为酒店
     */
    private int applyType;
    /**
     * 跳转地址
     */
    private String msgLink;
    /**
     * 消息状态
     * 0：未查看
     * 1：已查看
     */
    private Integer status;

    /**
     * 原因
     */
    private String content;

    private Integer minutes;

    private OffsetDateTime supplementTimeEnd;

    private OffsetDateTime supplementTime;

    private String requestId;
    /**
     * 是否已查看0未查看1已查看
     */
    private Integer checkSign = 0;

    /**
     * 消息类型1补签申请2加时申请3请假申请4调配申请5绑定申请6任务推荐9通知
     */
    private Integer messageType = 0;

    /**
     * 是否需要处理0任务1(绑定解绑通知)
     */
    private Integer isTask = 0;
    /**
     * 任务id
     */
    private String taskId;
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
