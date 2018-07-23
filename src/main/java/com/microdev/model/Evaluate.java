package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("evaluate")
public class Evaluate extends BaseEntity{

    private Integer level;

    private String text;

    private Integer type;
}