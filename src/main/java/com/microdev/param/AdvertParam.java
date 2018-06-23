package com.microdev.param;

import lombok.Data;

@Data
public class AdvertParam {
    private String title;

    private String description;

    private String theCover;

    private String content;

    private Integer status;

    private String externalLinks;

    private Integer advertType;

    private String pid;

    private String location;
}
