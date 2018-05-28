package com.microdev.param.api.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkLogResponse {
    /**
     * 签到时间
     */
    private OffsetDateTime punchIn;

    /**
     * 签退时间
     */
    private OffsetDateTime punchOut;

    /**
     * 用餐次数
     */
    private Integer repastTimes;
}
