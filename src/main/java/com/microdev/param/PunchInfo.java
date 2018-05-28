package com.microdev.param;

import lombok.Data;

import java.time.OffsetTime;

@Data
public class PunchInfo {

    private OffsetTime startTime;
    private OffsetTime endTime;
    private String status;
    private int employerConfirmStatus;
    //大于7天用1表示
    private int expire;
}
