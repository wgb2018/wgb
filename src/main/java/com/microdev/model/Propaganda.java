package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("Propaganda")
public class Propaganda {
    private String name;
    private Integer worker;
    private Integer hr;
    private Integer hotel;
    private Integer total;
    private String leader;
}
