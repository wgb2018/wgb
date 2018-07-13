package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("Propaganda")
public class Propaganda {
    private String id;
    private OffsetDateTime createTime;
    private OffsetDateTime modifyTime;
    private Integer worker;
    private Integer hr;
    private Integer hotel;
    private Integer total;
    private String leader;
}
