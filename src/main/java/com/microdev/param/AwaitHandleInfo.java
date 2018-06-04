package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AwaitHandleInfo {

    private String taskTypeText;
    private String hourlyPay;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private OffsetDateTime dayStartTime;
    private OffsetDateTime dayEndTime;
    private String hotelName;
    private String address;
    private String confirmedWorkers;
    private String needWorkers;
    private String lackWorkers;
    /**
     * 1正在做的任务2等待做的任务3派单中的任务4等待派单的任务5等待接单的任务6等待处理的任务7新任务 8申请调换的任务9申请取消的任务10拒绝接单的任务11申请补签的任务12申请请假的任务13申请加时的任务14申请调配的任务
     */
    private String type;
    private String logo;
    private String content;
    private String messageId;
    private Integer isTask;
}
