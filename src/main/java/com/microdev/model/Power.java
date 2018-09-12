package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("power")
public class Power extends BaseEntity{

    private Integer code;
    //权限级别
    private Integer level;
    private String name;
    private String identifer;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;

    @TableField(exist = false)
    private String menuName;
}
