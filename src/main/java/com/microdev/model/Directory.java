package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("directory")
public class Directory extends BaseEntity{
    private String title;

    private String location;

    private String content;

    private Integer status;

}
