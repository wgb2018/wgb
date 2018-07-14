package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.*;

//人力资源公司任务数据
@Data
public class TaskHrCompanyViewDTO {
    /**
     * 主键
     */
    private String pid;
    private String taskHrId;
    private String hrCompanyId;
    /**
     * 人力资源公司
     */
    private String hrCompanyName;
    /**
     * 人力公司每小时薪资
     */
    private double hourlyPay;
    /**
     * 需要的人数
     */
    private Integer needWorkers;
    /**
     * 已确定接单的人数
     */
    private Integer confirmedWorkers;
    /**
     * 拒绝接单的人数
     */
    private Integer refusedWorkers;
    /**
     * 任务状态
     * 0：新任务等待派发
     * 1：已派发
     */
    private Integer status;

    private TaskViewDTO task;
    /**
     * 接受人列表
     */
    private List<Map<String, Object>> confirmedList=new ArrayList<>();
    /**
     * 决绝人列表
     */
    private List<Map<String, Object>> refusedList=new ArrayList<>();
    private List<Map<String, Object>> distributedList=new ArrayList<>();

    // 用人单位信息
    /**
     * 用人单位任务主键
     */
    private String hotelTaskId;
    /**
     * 用人单位名称
     */
    private String hotelName;
    /**
     * 用人单位地址
     */
    private String address;
    /**
     * 用人单位每小时薪资
     */
    private double hotelHourlyPay;
    /**
     * 任务类型文本
     */
    private String taskTypeText;
    /**
     * 任务内容（describe是关键字）
     */
    private String taskContent;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy.MM.dd")
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy.MM.dd")
    private OffsetDateTime toDate;


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


    /**
     *  人力公司付款给小时工金额  [  工作时间(时) * 时薪  ]
     */
    private Double workersShouldPay=0.0;
    /**
     *  人力公司已付小时工金额 (这个字段不清楚何时赋值,因为系统暂时是线下支付)
     */
    private Double workersHavePay=0.0;
    /**
     * 人力公司代付小时工金额（等于 workersPaySum-workersHaveSum）
     */
    private Double workersWaitPay=0.0;

    private String payStatus="";

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTaskHrId() {
        return taskHrId;
    }

    public void setTaskHrId(String taskHrId) {
        this.taskHrId = taskHrId;
    }

    public String getHrCompanyId() {
        return hrCompanyId;
    }

    public void setHrCompanyId(String hrCompanyId) {
        this.hrCompanyId = hrCompanyId;
    }

    public String getHrCompanyName() {
        return hrCompanyName;
    }

    public void setHrCompanyName(String hrCompanyName) {
        this.hrCompanyName = hrCompanyName;
    }

    public Integer getNeedWorkers() {
        return needWorkers;
    }

    public void setNeedWorkers(Integer needWorkers) {
        this.needWorkers = needWorkers;
    }

    public Integer getConfirmedWorkers() {
        return confirmedWorkers;
    }

    public void setConfirmedWorkers(Integer confirmedWorkers) {
        this.confirmedWorkers = confirmedWorkers;
    }

    public Integer getRefusedWorkers() {
        return refusedWorkers;
    }

    public void setRefusedWorkers(Integer refusedWorkers) {
        this.refusedWorkers = refusedWorkers;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public TaskViewDTO getTask() {
        return task;
    }

    public void setTask(TaskViewDTO task) {
        this.task = task;
    }

    public String getHotelTaskId() {
        return hotelTaskId;
    }

    public void setHotelTaskId(String hotelTaskId) {
        this.hotelTaskId = hotelTaskId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getHotelHourlyPay() {
        return hotelHourlyPay;
    }

    public void setHotelHourlyPay(double hotelHourlyPay) {
        this.hotelHourlyPay = hotelHourlyPay;
    }

    public String getTaskTypeText() {
        return taskTypeText;
    }

    public void setTaskTypeText(String taskTypeText) {
        this.taskTypeText = taskTypeText;
    }

    public String getTaskContent() {
        return taskContent;
    }

    public void setTaskContent(String taskContent) {
        this.taskContent = taskContent;
    }

    public OffsetDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(OffsetDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public OffsetDateTime getToDate() {
        return toDate;
    }

    public void setToDate(OffsetDateTime toDate) {
        this.toDate = toDate;
    }

    public Integer getRepastTimes() {
        return repastTimes;
    }

    public void setRepastTimes(Integer repastTimes) {
        this.repastTimes = repastTimes;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Double getShouldPayMoney() {
        return shouldPayMoney;
    }

    public void setShouldPayMoney(Double shouldPayMoney) {
        this.shouldPayMoney = shouldPayMoney;
    }

    public Double getHavePayMoney() {
        return havePayMoney;
    }

    public void setHavePayMoney(Double havePayMoney) {
        this.havePayMoney = havePayMoney;
    }

    public Double getWaitPayMoney() {
        return waitPayMoney;
    }

    public void setWaitPayMoney(Double waitPayMoney) {
        this.waitPayMoney = waitPayMoney;
    }

    public Double getWorkersShouldPay() {
        return workersShouldPay;
    }

    public void setWorkersShouldPay(Double workersShouldPay) {
        this.workersShouldPay = workersShouldPay;
    }

    public Double getWorkersHavePay() {
        return workersHavePay;
    }

    public void setWorkersHavePay(Double workersHavePay) {
        this.workersHavePay = workersHavePay;
    }

    public Double getWorkersWaitPay() {
        return workersWaitPay;
    }

    public void setWorkersWaitPay(Double workersWaitPay) {
        this.workersWaitPay = workersWaitPay;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }
}
