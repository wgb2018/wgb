package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Data
public class EnrollerResponse {
    private String userId;

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

    @JsonFormat(pattern="yyyy.MM.dd HH:mm:ss")
    private OffsetDateTime applyTime;

    private Long applyTimeL;
}
