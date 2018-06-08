package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.param.InformType;
import lombok.Data;

@Data
@TableName("inform_template")
public class InformTemplate extends BaseEntity {
    private InformType code;

    private String content;

    private Integer sendType;

    private String title;

}
