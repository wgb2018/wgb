package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@TableName("notice")
public class Notice extends BaseEntity {
    private Integer needWorkers;

    private Long createTimeL;

    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime fromDate;

    @JsonFormat(pattern="yyyy.MM.dd")
    private OffsetDateTime toDate;

    private String content;

    private String hotelId;

    @TableField(exist = false)
    private List<Dict> service;
    @TableField(exist = false)
    private Company hotel;

    private String hrCompanyId;

    private Integer type;

    private String taskId;

    private Integer status;

    private Integer hrNeedWorkers;

    private Integer confirmedWorkers=0;
}
