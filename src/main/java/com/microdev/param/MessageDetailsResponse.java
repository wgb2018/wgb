package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MessageDetailsResponse {

    private String id;
    private String name;
    private String companyName;
    private String mobile;
    private String age;
    private String messageType;
    private String createTime;
    private String area;
    private String logo;
    private String minutes;
    private String content;
    private int amount;
    private OffsetDateTime supplementTime;
    private OffsetDateTime supplementTimeEnd;
    private int needWorkers;
    private int confirmedWorkers;
}
