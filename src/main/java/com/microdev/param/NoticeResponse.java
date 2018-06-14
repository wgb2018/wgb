package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NoticeResponse {

    private String pid;
    private OffsetDateTime createTime;
    private Long time;
    private String content;
    private String title;
    private String status;
}
