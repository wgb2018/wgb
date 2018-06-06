package com.microdev.param;


import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 创建酒店任务
 */
@Data
public class CreateTaskRequest {
    /**
     * 酒店Id
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
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;
    /**
     * 任务每日开始时间
     */
    private OffsetTime dayStartTime;
    /**
     * 任务每日截止时间
     */
    private OffsetTime dayEndTime;
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
}
