package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;

import java.time.OffsetDateTime;
import java.util.List;

@TableName("notice")
public class Notice extends BaseEntity {
    private Integer needworkers;

    private OffsetDateTime fromDate;

    private OffsetDateTime toDate;

    private String content;

    private String hotelId;

    @TableField(exist = false)
    private List<Dict> service;

}
