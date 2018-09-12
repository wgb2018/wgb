package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MessageDetailsResponse {

    private String pid;
    private String name;
    private String companyName;
    private String mobile;
    private String workerMobile;
    private int age;
    private String messageType;
    private String messageTextType;//申请类型名称
    private String createTime;
    private String area;
    private String logo;
    private String avatar;
    private String content;
    private int needWorkers;
    private int confirmedWorkers;
    private String address;
    private double money;//支付金额
    private String leaveStartDate;//请假开始日期
    private String leaveEndDate;//请假结束日期
    private String leaveStartTime;//请假开始时间
    private String leaveEndTime;//请假结束时间
    private String sex;
    private int allocateNum;//调配人数
    private String overtimeDate;//加时日期
    private int overtimeLength;//加时时长
    private String retroactiveTime;//补签时间
    private String retroactiveDate;//补签日期
    private String originator;//发起人
    private String taskHrId;//人力任务id
    private String workerTaskId;//小时工任务id
    private String messageCode;//服务类型码
    private String taskHotelId;//用人单位任务id
}
