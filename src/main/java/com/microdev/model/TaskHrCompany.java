package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


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
    private double HourlyPay;
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
     * 4：派发中
     * 5: 申请调配
     * 6: 等待做
     */
    private Integer status;
    /**
     * 拒绝理由
     */
    private String refusedReason;
    /**
     * 关联的酒店任务
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
     * 酒店
     */
    private String hotelId;

    private String hotelName;
    @TableField(exist = false)
    private Company hotel;
	private Double hourlyPayHotel;
    /**
     * 人力资源公司对应多个TaskWorker
     */
    @TableField(exist = false)
    private List<String> listWorkerTask = new ArrayList<String>();
    /**
     * 就餐次数。就餐打卡累积这个数字（应该注意：就餐打卡的时间太近是按一次计算还是两次计算）
     */
    private Integer repastTimes=0;
    /**
     * 工作累积分钟数，每次签退,累积这个数字
     */
    private Integer minutes=0;
    /**
     *  酒店应付人力公司金额  [  工作时间(时) * 时薪  ]
     */
    private Double shouldPayMoney=0.0;
    /**
     *  酒店已付人力公司金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double havePayMoney=0.0;
    /**
     *  人力公司付款给小时工金额  [  工作时间(时) * 时薪  ]
     */
    private Double workersShouldPay=0.0;
    /**
     *  人力公司已付小时工金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double workersHavePay=0.0;
    /**
     *  任务描述
     */
    private String taskContent;

    /**
     * 是否已查看0未读1未完成已读3已完成已读
     */
    private Integer checkSign = 0;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getListWorkerTask() {
        return listWorkerTask;
    }

    public void setListWorkerTask(List<String> listWorkerTask) {
        this.listWorkerTask = listWorkerTask;
    }


    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

}
