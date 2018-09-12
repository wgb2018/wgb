package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TaskViewDTO {
    /**
     * 主键
     */
    private String pid;
    /**
     * 用人单位名称
     */
    private String hotelName;
    /**
     * 用人单位地址
     */
    private String address;
    /**
     * 任务类型文本
     */
    private String taskTypeText;
    /**
     * 任务内容（describe是关键字）
     */
    private String taskContent;
    /**
     * 开始时间
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;
    /**
     * 每小时薪资
     */
    private double hourlyPay;
    /**
     * 需要的人数
     */
    private double needWorkers;

    /**
     * 已确定接单的人数
     */
    private Integer confirmedWorkers;
    /**
     * 已拒绝接单的人数
     */
    private Integer refusedWorkers;
    /**
     * 包含的人力资源公司
     */
    private List<TaskHrCompanyViewDTO> listTaskHr = new ArrayList<>();

    /**
     * 包含的人力资源公司
     */
    private TaskWorkerViewDTO listTaskWorker = new TaskWorkerViewDTO();

    //结账相关的

    /**
     * 就餐次数。就餐打卡累积这个数字（应该注意：就餐打卡的时间太近是按一次计算还是两次计算）
     */
    private Integer repastTimes=0;
    /**
     * 工作累积分钟数，每次签退,累积这个数字
     */
    private Integer minutes=0;
    /**
     * 日任务开始时间
     */
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    /**
     * 日任务结束时间
     */
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;
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

    private Double unConfirmedPay=0.0;

    private String payStatus;
    /**
     * 任务状态
     */
    private Integer status;

    private Integer settlementPeriod;

    private Integer settlementNum;

    private Integer workerSettlementPeriod;

    private Integer workerSettlementNum;


}
