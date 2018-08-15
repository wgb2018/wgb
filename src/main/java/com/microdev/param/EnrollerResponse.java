package com.microdev.param;

import lombok.Data;

import java.time.OffsetTime;

@Data
public class EnrollerResponse {
    private String pid;

    private String workerId;

    private String workerName;

    private String avatar;

    private String sex;

    private String age;

    private String hrId;

    private String hrName;

    private String logo;

    private String applyWorkers;

    private String assignWorkers;

    private String mobile;

    private OffsetTime applyTime;

    private Long applyTimeL;
}
