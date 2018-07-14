package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class TaskDTO {
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
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;
    /**
     * 每小时薪资
     */
    private double hourlyPay;
    /**
     * 用人单位Id
     */
    private String hotelId;
    /**
     * 指定的人力资源公司
     */
    private Set<TaskHrCompanyDTO> hrCompanySet= new HashSet<TaskHrCompanyDTO>();
    /**
     * 指定任务的日工作开始时间
     */
    private OffsetTime dayStartTime;
    /**
     * 指定任务的日工作结束时间
     */
    private OffsetTime dayEndTime;
}
