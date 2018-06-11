package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkerCancelTask {

    private String workerId;
    private String userId;
    private String username;
    private String hotelId;
    private String hrId;
    private String hotelTaskId;
    private String taskHrId;
    private String taskId;
    private OffsetDateTime dayStartTime;
    private OffsetDateTime dayEndTime;
}
