package com.microdev.param;

import lombok.Data;

import java.util.List;

@Data
public class CreateNoticeRequest {

    private List<String> service;

    private Long fromDateL;

    private Long toDateL;

    private String content;

    private String hotelId;

    private Integer needWorkers;

}
