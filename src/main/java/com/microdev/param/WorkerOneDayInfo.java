package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Date;

@Data
public class WorkerOneDayInfo {

    private String fromDate;
    private String toDate;
    private String status;
    private String id;
    private String employerConfirmStatus;
    private OffsetDateTime createTime;
    private int totalTime;
}
