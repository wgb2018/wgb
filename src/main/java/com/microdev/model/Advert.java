package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("advertisement")
public class Advert extends BaseEntity{

    private String description;

    private String theCover;

    private String content;

    private Integer status = 0;

    private String externalLinks;

    private Integer advertType;

    private String title;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime releaseTime;

    private String location;
}
