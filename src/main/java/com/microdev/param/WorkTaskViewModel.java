package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

/**
 * @author yinbaoxin
 * 小时工任务详情
 */
@Data
public class WorkTaskViewModel {

    private String workTaskId;
    private String workerId;
    private String hrCompanyName;

    private String hotelName;
    private String hotelAddress;
    private String hotelLeader;
    private String hotelMobile;
    private String hrLeader;
    private String hrMobile;
    /**
     * 任务类型名称
     */
    private String taskTypeText;
    /**
     * 任务描述
     */
    private String taskContent;
    /**
     * 开始时间
     */
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;

    private OffsetTime dayStartTime;

    private OffsetTime dayEndTime;
    /**
     * 每小时薪资
     */
    private Double hourlyPay;
    //任务状态
    private Integer status;
    /**
     * 拒绝理由
     */
    private String refusedReason;

    /**
     * 就餐次数
     */
    private Integer repastTimes=0;
    /**
     * 工作累积分钟数，每次签退,累积这个数字
     */
    private Integer minutes=0;
    /**
     *  应付金额  [  工作时间(时) * 时薪  ]
     */
    private Double shouldPayMoney=0.0;
    /**
     *  已付金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double havePayMoney=0.0;
    /**
     * 代付金额（等于 shouldPay-havePay）
     */
    private Double waitPayMoney=0.0;
    private String payStatus="";
    private Double unConfirmedPay=0.0;
    private Integer settlementPeriod;
    private Integer settlementNum;
}
