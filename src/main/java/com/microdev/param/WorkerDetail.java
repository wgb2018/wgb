package com.microdev.param;

import lombok.Data;

import java.util.List;

@Data
public class WorkerDetail {

    private String time;

    private List<PunchInfo> workList;
}
