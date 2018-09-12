package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 用人单位任务
 *
 * @author yinbaoxin
 */
@Data
@TableName("task")
public class Task extends BaseEntity {
    /**
     * 任务类型
     */
    private String taskTypeCode;

    private String taskTypeText;
    /**
     * 任务内容（describe是关键字）
     */
    private String taskContent;
    /*
     * 开始时间
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;
    /**
     * 任务每日开始时间
     */
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    /**
     * 任务每日截止时间
     */
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;
    /**
     * 每小时薪资
     */
    private Double hourlyPay;
    @TableField(exist = false)
    private Double hourlyPayHotel;
    /**
     * 需要的总人数
     */
    private Integer needWorkers=0;
    /**
     * 任务状态
     * 1：人力未接单 ；2 ：人力已接单 ；3： 人力派单中； 4 ：派单完成； 5 ：正在做； 6 ：已完成。  8：人力公司拒绝接单
     */
    private Integer status;

    /**
     * 已确定接单的人数
     */
    private Integer confirmedWorkers=0;
    /**
     * 已拒绝接单的人数
     */
    private Integer refusedWorkers=0;

    /**
     * 用人单位信息
     */
    private String hotelId;

    private String hotelName;
    /**
     * 用人单位任务对应多个HrTask
     */
    @TableField(exist = false)
    private List<String> listHrTask = new ArrayList<String>();
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

    private Double unConfirmedPay=0.0;

    /**
     * 0代表天，1代表月
     */
    private Integer settlementPeriod;

    private Integer settlementNum;

    private Integer WorkerSettlementPeriod;

    private Integer WorkerSettlementNum;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    public List<String> getListHrTask() {
        return listHrTask;
    }

    public void setListHrTask(List<String> listHrTask) {
        this.listHrTask = listHrTask;
    }


    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

}
