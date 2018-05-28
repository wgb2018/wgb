package com.microdev.param.api.response;


import com.microdev.param.PunchType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GetCurrentTaskResponse {
    private String hotelId;
    private String hotelName;
    private String hotelLogo;
    private String hotelAddress;

    private String hrCompanyId;
    private String hrCompanyName;

    private String taskId;
    private String taskWorkerId;
    private Integer taskWorkerStatus;
    private Double taskWorkerHours;
    private String taskType;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private String taskContent;
    private List<WorkLogResponse> workLogs;
    //打卡类型
    private PunchType needPunchType;
}
