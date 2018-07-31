package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AwaitHandleInfo {

    private String taskTypeText;
    private Double hourlyPay;
    private String fromDate;
    private String toDate;
    private String dayStartTime;
    private String dayEndTime;
    private String hotelName;
    private String address;
    private String confirmedWorkers;
    private String needWorkers;
    private String lackWorkers;
    private String hrNeedWorkers;//人力所需人数
    /**
     * 消息类型1补签申请2加时申请3请假申请4调配申请5绑定申请6新任务7申请取消任务8收入确认9申请替换10拒绝接单* 11待派单12申请解绑13申请合作
     */
    private Integer type;
    private String logo;
    private String content;
    private String messageId;
    private Integer isTask;
    private String area;
    private String hrConfirmedWorkers;
    private String taskContent;
    private String hotelLeader;
    private String amount;
    private String hotelLeaderMobile;
    @JsonFormat(pattern="yyyy.MM.dd hh:mm:ss")
    private OffsetDateTime createTime;
    /**
     *消息发送者类型1小时工2人力公司3用人单位4系统
     */
    private int applicantType;
    /**
     * 申请类型0代表人力公司派发任务给小时工，1代表被申请方为小时工，2代表被申请方为人力资源公司，      *  3代表被申请方为用人单位
     */
    private int applyType;
}
