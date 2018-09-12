package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("menu")
public class Menu extends BaseEntity{

    //菜单名称
    private String name;
    //菜单类型
    private Integer level;
    private String agentId;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    //0启用1禁用
    private Integer status;
}
