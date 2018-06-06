package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AwaitTaskResponse {

    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private OffsetDateTime dayStartTime;
    private OffsetDateTime dayEndTime;
    private String taskContent;
    private String needWorkers;
    private String name;//酒店名称
    private String address;//酒店地址
    private String leader;//酒店负责人
    private String leaderMobile;//负责人联系电话
    private String hourlyPay;//人力每小时支付
    private String hourlyPayHotel;//酒店每小时支付
    private String messageTitle;
    private String messageType;
    private String hrName;
    private String hotelId;
    private String hrId;
    private String hrLeader;
    private String hrLeaderMobile;
    private String confirmedWorkers;//已报名人数
    private String area;//酒店区域
    private String taskId;//任务id
}
