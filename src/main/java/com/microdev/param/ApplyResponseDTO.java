package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Data
public class ApplyResponseDTO {

    private String messageId;
    private String content;//调配原因
    private String amount;//数量
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private OffsetTime dayStartTime;
    private OffsetTime dayEndTime;
    private String taskTypeText;//服务类型
    private String taskContent;//任务内容
    private Integer status;
    private Double hourlyPayHotel;
    private String hrName;
    private String hrLeader;
    private String hrLeaderMobile;
    private String logo;
    private String address;//地址
    private String area;//区域
    private String hotelName;//用人单位名称
    private String hotelLeader;//用人单位负责人
    private String hotelLeaderMobile;//用人单位负责人电话
    private String username;//小时工昵称
    private int age;//小时工年龄
    private String userMobile;//小时工电话
    private String sex;//小时工性别
    private String messageType;//消息类型
    private OffsetDateTime createTime;
}
