package com.microdev.param;

import lombok.Data;

@Data
public class HotelHandleWorkerRecord {

    private String date;
    private String taskWorkerId;
    private String status;//1早退2迟到3旷工
}
