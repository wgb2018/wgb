package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
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

    private String taskHrId;
    /**
     * 人力公司ID
     */
    private String hrCompanyId;
    /**
     * 人力公司名称
     */
    @TableField(exist = false)
    private String hrCompanyName;

    @TableField(exist = false)
    private String hrCompanyLogo;
    /**
     * 酒店ID
     */
    private String hotelId;
    /**
     * 酒店名称
     */
    @TableField(exist = false)
    private String hotelName;

    @TableField(exist = false)
    private String hotelLogo;
    /**
     * 小时工ID
     */
    private String workerId;
    /**
     * 小时工名称
     */
    @TableField(exist = false)
    private String workerName;

    @TableField(exist = false)
    private String workerAvatar;
    /**
     * 支付类型
     * 1酒店支付人力公司
     * 2人力公司支付小时工
     */
    private Integer payType;
    /**
     * 支付状态
     * 0 未确认
     * 1 同意
     * 2 拒绝
     */
    private Integer status;
}
