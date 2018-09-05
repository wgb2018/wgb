package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.microdev.model.Dict;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;

@Data
public class NoticeDetails {
    private String userId;

    private boolean available;

    private String hourlyPay;

    private String needWorkers;

    private String confirmedWorkers;

    private String enrollWorkers;

    private String taskContent;

    private String hrLeaderMobile;

    private String hrLeader;

    private String hotelLeaderMobile;

    private String hotelLeader;

    private String hrcompanyId;

    private String hotelId;

    private String hrCompanyName;

    private String hrLogo;

    private String hotelLogo;

    private String hotelName;

    private String hrAddress;

    private String hrArea;

    private String hrAddressCode;

    private String hotelAddress;

    private String hotelArea;

    private String hotelAddressCode;

    private Long createTime;
    //任务返回字段
    private String taskTypeText;

    private String settlementPeriod;

    private String settlementNum;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;

    //招聘返回字段

    private List<Dict> TaskServices;

    private String sex;

    private String education;

    private String stature;

    private String weight;

    private String healthcard;

}
