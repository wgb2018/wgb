package com.microdev.param;

import lombok.Data;

/**
 * 人力公司支付小时工的请求
 */
@Data
public class WorkerPayDetailRequest {
    /**
     * 任务Id
     */
    private String taskWorkerId;
    /**
     * 支付金额,
     */
    private Double payMoney;
}
