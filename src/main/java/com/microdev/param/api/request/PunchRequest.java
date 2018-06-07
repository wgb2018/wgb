package com.microdev.param.api.request;

import com.microdev.param.Measure;
import com.microdev.param.PunchType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PunchRequest {
    /**
     * 打卡类型
     */
    private PunchType punchType;
    /**
     * 打卡对应的任务
     */
    private String taskWorkerId;
    /**
     * 打卡时间
     */
    private OffsetDateTime punchTime;

    private Long punchTimeL;

    private Measure measure;

}
