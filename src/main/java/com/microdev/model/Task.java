package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 酒店任务
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
    /**
     * 开始时间
     */
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;
    /**
     * 任务每日开始时间
     */
    private OffsetTime dayStartTime;
    /**
     * 任务每日截止时间
     */
    private OffsetTime dayEndTime;
    /**
     * 每小时薪资
     */
    private double hourlyPay;
    /**
     * 需要的总人数
     */
    private double needWorkers=0;
    /**
     * 任务状态
     * 1：人力未结单
     * 2: 人力已结单
     * 3: 人力派单中
     * 4: 派单完成
     * 5：等待做
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
     * 酒店信息
     */
    private String hotelId;

    private String hotelName;
    /**
     * 酒店任务对应多个HrTask
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

    /**
     * 是否已查看0未查看1已查看
     */
    private Integer checkSign = 0;

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
