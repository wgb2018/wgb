package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 任务派发
 */
@Data
public class HrTaskDistributeRequest {
    /**
     * 主键
     */
    private String id;
    /**
     * 人力公司每小时薪资
     */
    private Double hourlyPay;
    /**
     * 指定的员工id
     */
    private Set<String> workerIds= new HashSet<String>();
}
