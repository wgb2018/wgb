package com.microdev.param;

import lombok.Data;

/**
 * 酒店支付人力公司的请求
 */
@Data
public class HrPayDetailRequest {
    /**
     * 任务Id
     */
    private String taskHrId;
    /**
     * 本次付款金额
     */
    private double thisPayMoney;
}
