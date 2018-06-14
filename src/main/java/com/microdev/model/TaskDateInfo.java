package com.microdev.model;

import lombok.Data;

import java.util.Date;

@Data
public class TaskDateInfo {

    private Date fromDate;
    private Date toDate;
    private Date dayStartTime;
    private Date dayEndTime;
}
