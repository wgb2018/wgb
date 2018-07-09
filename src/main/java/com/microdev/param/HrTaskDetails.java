package com.microdev.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.List;
import java.util.Map;

@Data
public class HrTaskDetails {
    private Double hourlyPayHotel;
    private String hotelId;
    private String hotelName;
    private String leaderMobile;
    private String leader;
    private String hotelTaskId;
    private String taskTypeText;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayStartTime;
    @JsonFormat(pattern="HH:mm")
    private OffsetTime dayEndTime;
    private String taskContent;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;
    private String hotelAddress;
    private Double hotelHourlyPay;
    private Integer distributeWorkers;
    private String hrCompanyId;
    private String hrCompanyName;
    private String pid;
    private Double hourlyPay;
    private Integer needWorkers;
    private Integer confirmedWorkers;
    private Integer refusedWorkers;
    private Integer status;
    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime repastTimes;
    private Double minutes;
    private Double shouldPayMoney;
    private Double havePayMoney;
    private Double waitPayMoney;
    private Double unConfirmedPay=0.0;
    private Double workerUnConfirmed=0.0;
    private Double workersHavePay;
    private Double workersShouldPay;
    private Double workersWaitPay;
    private String area;
    private String payStatus;
    private List<Map<String, Object>> confirmedSet;
    private List<Map<String, Object>> refusedSet;
    private List<Map<String, Object>> distributedSet;
    private Integer settlementPeriod;
    private Integer settlementNum;
    private Integer workerSettlementPeriod;
    private Integer workerSettlementNum;

}
