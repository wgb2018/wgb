package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.microdev.model.Dict;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EnrollDetails {
    private String userId;

    private String taskTypeText;

    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;

    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;

    private String hotelId;

    private String hotelName;

    private String hotelLogo;

    private String needWorkers;

    private String hourlyPay;

    private String hrId;

    private String hrComapnyName;

    private String hrComapnyLogo;

    private String noticeId;
    // 0待接受 1已同意 2已拒绝
    private Integer status;

    private Integer type;

    private List<Dict> service;

    @JsonFormat(pattern="yyyy.MM.dd HH:mm")
    private OffsetDateTime enrollTime;

    @JsonFormat(pattern="yyyy.MM.dd HH:mm")
    private OffsetDateTime handleTime;

}
