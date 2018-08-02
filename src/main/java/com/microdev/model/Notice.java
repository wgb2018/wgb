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
    //1:用人单位发布任务给人力  2:用人单位招聘小时工 3:人力发布任务给小时工 4:人力招聘小时工
    private Integer type;

    private String taskId;
    //0:活跃 1:过期
    private Integer status;

    private Integer confirmedWorkers=0;
}
