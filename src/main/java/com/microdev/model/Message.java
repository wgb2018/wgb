package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
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
     * 用人单位id
     */
    private String hotelId;
    /**
     * 人力资源公司id
     */
    private String hrCompanyId;
    /**
     *消息发送者类型1小时工2人力公司3用人单位4系统
     */
    private int applicantType;
    /**
     * 申请类型0代表人力公司派发任务给小时工，1代表被申请方为小时工，2代表被申请方为人力资源公司，      *  3代表被申请方为用人单位
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

    private String minutes;

    private OffsetDateTime supplementTimeEnd;

    private OffsetDateTime supplementTime;

    private String requestId;

    /**
     * 消息类型1补签申请2加时申请3请假申请4调配申请5绑定申请6新任务7申请取消任务8收入确认9申请替换10拒绝接单* 11待派单12申请解绑13申请合作14申请报名15申请绑定(小时工通过公告申请)
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
    /**
     * 人力任务id
     */
    private boolean isStop = false;
    private String hrTaskId;
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
