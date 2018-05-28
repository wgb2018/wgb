package com.microdev.param;

import lombok.Data;

/**
 * 人力资源公司派发的任务中，人员列表数据
 */
@Data
public class HrTaskWorkersResponse {

    private String taskWorkerId;
    /**
     * 小时工id
     */
    private String workerId;
    /**
     * 小时工姓名
     */
    private String workerName;
    private String headImage;
    /**
     * 小时工
     */
    private String mobile;
    /**
     * 小时工性别
     */
    private String Gender;

    /**
     * 小时工年龄
     */
    private Integer Age;
    /**
     * 小时工任务接受还是拒绝
     * * 1：接收
     * 2：拒绝
     */
    private Integer taskStatus;
    /**
     * 拒绝理由
     */
    private String refusedReason;

    //违约
    private boolean noPromise=false;
    //结账相关的

    /**
     * 就餐次数。就餐打卡累积这个数字（应该注意：就餐打卡的时间太近是按一次计算还是两次计算）
     */
    private Integer repastTimes=0;
    /**
     * 工作累积分钟数，每次签退,累积这个数字
     */
    private Integer minutes=0;
    /**
     *  应付金额  [  工作时间(时) * 时薪  ]
     */
    private Double shouldPayMoney=0.0;
    /**
     *  已付金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double havePayMoney=0.0;
    /**
     * 代付金额（等于 shouldPay-havePay）
     */
    private Double waitPayMoney=0.0;

}
