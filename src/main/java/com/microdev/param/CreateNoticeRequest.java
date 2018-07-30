package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CreateNoticeRequest {

    private List<String> service;

    private Long fromDateL;

    private Long toDateL;

    private String content;

    private String hotelId;

    private Integer needWorkers;

    private Integer hrNeedWorkers;

    private String taskContent;

    private Double hourlyPay;

    private Integer settlementPeriod;

    private Integer settlementNum;

    private Integer workerSettlementPeriod;

    private Integer workerSettlementNum;

    private String taskTypeText;

    private String taskTypeCode;

    //公告类型 1：酒店发布
    private Integer type;
    /**
     * 指定的人力资源公司
     */
    private Set<TaskHrCompanyDTO> hrCompanySet= new HashSet<TaskHrCompanyDTO> ();

    private Set<String> workerSet = new HashSet<String> ();

}
