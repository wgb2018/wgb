package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ApplySupplementRequest {

    private String messageId;
    private String taskTypeText;
    private String content;
    private String messageType;
    private OffsetDateTime supplementTime;
    private OffsetDateTime supplementTimeEnd;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private OffsetDateTime dayStartTime;
    private OffsetDateTime dayEndTime;
    private String hotelName;
    private String address;
    private String logo;
    private String username;
    private String sex;
    private String mobile;
    private String taskContent;
    private String minutes;
    private OffsetDateTime createTime;
}
