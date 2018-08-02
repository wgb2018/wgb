package com.microdev.param;

import lombok.Data;

@Data
public class WorkerTask {

    private String name;
    private String leader;
    private String leaderMobile;
    private String hrName;
    private String taskContent;
    private String type;
    private String salary;
    private String workDate;
    private String workTime;
    private String refuseReason;
    private String status;
}
