package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class QueryNoticeRequest {
    private String date;

    private String service;

    private String hotelName;

    private OffsetDateTime fromDate;

    private OffsetDateTime Todate;

}
