package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AwaitTaskResponse {

    private String fromDate;
    private String toDate;
    private String dayStartTime;
    private String dayEndTime;
    private String taskContent;
    private String needWorkers;
    private String name;//用人单位名称
    private String address;//用人单位地址
    private String leader;//用人单位负责人
    private String leaderMobile;//负责人联系电话
    private String hourlyPay;//人力每小时支付
    private String hourlyPayHotel;//用人单位每小时支付
    private String messageTitle;//服务类型
    private String messageType;
    private String hrName;
    private String hotelId;
    private String hrId;
    private String hrLeader;
    private String hrLeaderMobile;
    private String confirmedWorkers;//已报名人数
    private String area;//用人单位区域
    private String messageId;
    private String hrAddress;//人力地址
    private String hrArea;//人力区域
    private String taskHotelId;//用人单位任务id
    private String taskHrId;//人力任务id
    private String workerTaskId;//小时工任务id
    private String taskTypeCode;//任务码
    private String taskTypeText;//任务类型
    private String settlementPeriod;
    private String settlementNum;
    private String workerSettlementPeriod;
    private String workerSettlementNum;
    private String billId;//账单id
    private Integer type;//小时工任务类型
    /*private String workerName;//小时工姓名
    private String age;//年龄
    private String sex;//性别
    private String mobile;//电话
    private String workerId;//小时工ID
    private String reason;//原因
    private OffsetDateTime createTime;//时间*/
}
