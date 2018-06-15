package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("advertisement")
public class Advert extends BaseEntity{

    private String description;

    private String theCover;

    private String content;

    private Integer status = 0;

    private String externalLinks;

    private Integer advertType;

    private String tittle;
}
