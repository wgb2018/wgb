package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 人力资源公司任务
 *
 * @author yinbaoxin
 */
@Data
@TableName("task_hr_company")
public class TaskHrCompany extends BaseEntity {
    /**
     * 任务类型
     */
    private String taskTypeCode;

    private String taskTypeText;
    /**
     * 人力公司每小时薪资
     */
    private double hourlyPay;
    /**
     * 需要的人数
     */
    private Integer needWorkers=0;
    /**
     * 已确定接单的人数
     */
    private Integer confirmedWorkers=0;
    /**
     * 已拒绝接单的人数
     */
    private Integer refusedWorkers=0;
    /**
     * 任务状态
     * 1：新任务等待确认
     * 2: 接受任务
     * 3: 拒绝任务
     * 4：已派发
     * 5: 派单完成
     * 6.执行中
     * 7.已完成
     * 8.终止
     */
    private Integer status;
    /**
     * 拒绝理由
     */
    private String refusedReason;
    /**
     * 关联的用人单位任务
     */

    private String taskId;
    /**
     * 人力资源公司
     */
    private String hrCompanyId;

    private String hrCompanyName;
	@TableField(exist = false)
    private Company hrCompany;
    /**
     * 用人单位
     */
    private String hotelId;

    private String hotelName;
    @TableField(exist = false)
    private Company hotel;
	private Double hourlyPayHotel;
    /**
     * 人力资源公司对应多个TaskWorker
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;
    @TableField(exist = false)
    private List<Map<String,Object>> listWorkerTask = new ArrayList<Map<String,Object>>();
    /**
     * 就餐次数。就餐打卡累积这个数字（应该注意：就餐打卡的时间太近是按一次计算还是两次计算）
     */
    private Integer repastTimes=0;
    /**
     * 工作累积分钟数，每次签退,累积这个数字
     */
    private Integer minutes=0;
    /**
     *  用人单位应付人力公司金额  [  工作时间(时) * 时薪  ]
     */
    private Double shouldPayMoney=0.0;
    /**
     *  用人单位已付人力公司金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double havePayMoney=0.0;
    /**
     *  人力公司付款给小时工金额  [  工作时间(时) * 时薪  ]
     */
    @TableField(exist = false)
    private Double paidPayMoney=0.0;
    /**
     *  人力公司付款给小时工金额  [  工作时间(时) * 时薪  ]
     */
    private Double workersShouldPay=0.0;
    /**
     *  人力公司已付小时工金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double workersHavePay=0.0;

    @TableField(exist = false)
    private Double workersPaidPay=0.0;

    private Double unConfirmedPay = 0.0;
    private Double workerUnConfirmed = 0.0;   

	 /**
     *  任务描述
     */
    private String taskContent;
    /**
     *  已分配人数
     */
    private Integer distributeWorkers = 0;

    private Integer settlementPeriod;

    private Integer settlementNum;

    private Integer workerSettlementPeriod;

    private Integer workerSettlementNum;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

}
