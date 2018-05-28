package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

/**
 * 账单
 *
 */
@Data
@TableName("bill")
public class Bill extends BaseEntity{
    /**
     * 支付金额
     */
    private Double payMoney=0.0;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 人力公司ID
     */
    private String hrCompanyId;
    /**
     * 人力公司名称
     */
    private String hrCompanyName;
    /**
     * 酒店ID
     */
    private String hotelId;
    /**
     * 酒店名称
     */
    private String hotelName;
    /**
     * 小时工ID
     */
    private String workerId;
    /**
     * 小时工名称
     */
    private String workerName;
    /**
     * 支付类型
     * 1酒店支付人力公司
     * 2人力公司支付小时工
     */
    private Integer payType;
}
