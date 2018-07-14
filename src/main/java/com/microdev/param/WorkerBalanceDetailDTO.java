package com.microdev.param;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小时工款项明细信息
 */
@Data
public class WorkerBalanceDetailDTO {
    /**
     * 款项金额
     */
    private BigDecimal amount;
    /**
     * 款项是否已结算
     */
    private boolean settled;
    /**
     * 款项对应的任务信息
     */
    private TaskDTO task;
    /**
     * 指派该任务的人力公司信息
     */
    private CompanyDTO hrCompany;
    /**
     * 发布该任务的用人单位信息
     */
    private CompanyDTO hotel;
}
