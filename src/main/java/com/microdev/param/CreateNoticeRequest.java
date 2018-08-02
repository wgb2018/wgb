package com.microdev.param;

import com.microdev.type.UserSex;
import lombok.Data;

import java.time.OffsetTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CreateNoticeRequest {

    private List<String> service;

    private Long fromDateL;

    private Long toDateL;

    /**
     * 任务每日开始时间
     */
    private Long dayStartTimeL;
    /**
     * 任务每日截止时间
     */
    private Long dayEndTimeL;

    private String content;

    private String hotelId;

    private String hrCompanyId;

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

    //公告类型 1：用人单位发布 2:人力发布
    private Integer type;
    /**
     * 指定的人力资源公司
     */
    private Set<TaskHrCompanyDTO> hrCompanySet= new HashSet<TaskHrCompanyDTO> ();

    private Set<String> workerSet = new HashSet<String> ();

    //人力招聘小时工参数
    private String stature;//身高
    private String weight;//体重
    private int education;//学历
    private UserSex sex;

}
