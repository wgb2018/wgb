package com.microdev.param;


import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 创建用人单位任务
 */
@Data
public class CreateTaskRequest {
    /**
     * 用人单位Id
     */
    private String hotelId;
    /**
     * 任务类型
     */
    private String taskTypeCode;
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
    private Long fromDateL;
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;
    private Long toDateL;
    /**
     * 任务每日开始时间
     */
    private OffsetTime dayStartTime;
    private Long dayStartTimeL;
    /**
     * 任务每日截止时间
     */
    private OffsetTime dayEndTime;
    private Long dayEndTimeL;
    /**
     * 每小时薪资
     */
    private double hourlyPay;
    /**
     * 指定的人力资源公司
     */
    private Set<TaskHrCompanyDTO> hrCompanySet= new HashSet<TaskHrCompanyDTO>();

    /**
     * 任务id
     */
    private String taskId;

    private String messageId;

    private String taskHrId;

    private Integer settlementPeriod;

    private Integer settlementNum;

    private Integer WorkerSettlementPeriod;

    private Integer WorkerSettlementNum;

    private Integer needhrCompanys;

    private boolean isNoticeTask = false;
}
