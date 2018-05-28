package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkerPunchDTO {
    /**
     * 打卡的小时工标识ID
     */
    private String workerId;
    /**
     * 打卡类型
     */
    private PunchType punchType;
    /**
     * 打卡对应的任务
     */
    private String taskId;
    /**
     * 打卡时间
     */
    private OffsetDateTime punchTime;
}
