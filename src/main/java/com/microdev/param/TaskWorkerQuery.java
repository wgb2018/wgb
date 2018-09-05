package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author yinbaoxin
 * 查找小时工任务的信息
 */
@Data
public class TaskWorkerQuery {

    private String workerId;

    private String userId;
    /**
     * 任务状态
     * 0：新任务等待确认
     * 1：已接受
     * 2：已拒绝
     * 3  终止
     * 4：进行中，根据时间判断是否进行中
     * 5：已结束，根据时间判断是否已结束
     */
    private Integer taskStatus;

    //0 查全部
    //1 未结算
    //2 结算中
    //3 已结算
    private Integer payStatus = 0;

    private String ofDate;

    private String taskTypeCode;

    private String hrCompanyName;

    private OffsetDateTime fromDate;

    private OffsetDateTime toDate;

    private String hotelName;

    private String hrCompanyId;

    private String hotelId;

    private String taskWorkerId;

    private String hrTaskId;

    private Integer type;

}
