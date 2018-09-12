package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

/**
 * 小时工任务
 *
 * @author yinbaoxin
 */
@Data
@TableName("task_worker")
public class TaskWorker extends BaseEntity {

    /**
     * 小时工标识ID
     */
    private String workerId;

	@TableField(exist = false)
    private User user;
    /**
     * ybx 小时工信息，关联出人员的信息
     */
    private String userId;

    private String userName;

    @TableField(exist = false)
    private String avatar;

    /**
     * ybx 指派该任务的人力公司信息。TaskHrCompany可以关联查询出用人单位的任务
     */
    private String taskHrId;

    /**
     * ybx 确定任务时间（接收或拒绝）
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime confirmedDate;
    /**
     * ybx 任务状态
     * 0：新任务等待确认
     * 1：接受
     * 2：拒绝
     * 3: 终止任务
     * 4: 进行中（1 到开始时间）
     * 5: 已完成
     * 11:违约
     */
    private Integer status=0;

    /**
     * ybx 拒绝理由
     */
    private String refusedReason;
    /**
     * ybx 违约
     */
    private boolean noPromise=false;
    /**
     * ybx 开始时间(重复字段，方便判断任务时间是否冲突)
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    /**
     * ybx 截止时间（(重复字段，方便判断任务时间是否冲突)）
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;

    /**
     * 任务是否已结算
     */
    private Boolean settled=false;
    /**
     * 任务结算时间
     */
    private OffsetDateTime settledDate;
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

    @TableField(exist = false)
    private Double paidPayMoney=0.0;

    private Double unConfirmedPay=0.0;
    /**
     *  每小时支付给小时工价钱
     */
    private Double hourlyPay;
    /**
     *  人力公司名称
     */
    private String hrCompanyId;
    private String hrCompanyName;
    @TableField(exist = false)
    private Company hrCompany;
    /**
     * 任务内容
     */
    private String taskTypeCode;

    private String taskTypeText;

    private String taskContent;
    /**
     * 任务类型 0：人力发给小时工任务
     */
    private Integer type = 0;
    /**
     *  用人单位名称
     */
    private String hotelName;

    private String hotelId;
    @TableField(exist = false)
    private Company hotel;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;
    private String hotelTaskId;
    private Integer settlementPeriod;
    private Integer settlementNum;
    /**
    * 是否验证
    * */
    private Integer verification;
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }



    public Boolean getSettled() {
        return settled;
    }

    public void setSettled(Boolean settled) {
        this.settled = settled;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }


}
