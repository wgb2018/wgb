package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TaskHrQueryDTO {
    /**
     * 用人单位ID
     */
    private String hotelId;

    private String hotelName;
    /**
     * 人力资源公司
     */
    private String hrCompanyId;
    /**
     * 人力资源公司名称
     */
    private String hrCompanyName;

    private String taskTypeText;

    //0 查全部
    //1 未结算
    //2 结算中
    //3 已结算
    private Integer payStatus;
    /**
     * 任务状态
     * 0：新任务等待确认
     * 1: 接受任务
     * 2: 拒绝任务
     * 3：已派发
     */
    private Integer status;
    /**
     * 任务开始时间
     */
    private OffsetDateTime fromDate;
    /**
     * 任务结束时间
     */
    private OffsetDateTime toDate;
    /**
     * 时薪上限
     */
    private Double payUp;
    /**
     * 时薪下限
     */
    private Double payDown;

    private String ofDate;

	private String taskId;

	private String taskTypeCode;
}
