package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 任务派发
 */
@Data
public class HrTaskDistributeRequest {
    private String hrTaskId;
    /**
     * 主键
     */
    private String messageId;
    /**
     * 人力公司每小时薪资
     */
    private Double hourlyPay;

    private Integer settlementPeriod;

    private Integer settlementNum;

    private boolean noticeTask = false;
    /**
     * 指定的员工id
     */
    private Set<String> workerIds= new HashSet<String>();
}
