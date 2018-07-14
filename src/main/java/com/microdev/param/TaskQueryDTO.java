package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;


@Data
public class TaskQueryDTO {
    /**
     * 任务Id
     */
    private String Id;
    /**
     * 用人单位Id
     */
    private String hotelId;
    /**
     * 用人单位名称
     */
    private String hotelName;
    /**
     * 任务类型
     */
    private String taskTypeText;
    //0 查全部
    //1 未结算
    //2 结算中
    //3 已结算
    private Integer payStatus;
    /**
     * 任务查询开始时间
     */
    private OffsetDateTime startTime;
    /**
     * 任务查询结束时间
     */
    private OffsetDateTime endTime;
    /**
     * 任务状态
     */
    private Integer status;
    /**
     * 任务类型
     */
    private String tasktype;

    private String taskTypeCode;
    /**
     * 任务时薪（上限和下限）
     */
    private Double payUp;

    private Double payDown;

    private String ofDate;

    private String hrCompanyName;


}
