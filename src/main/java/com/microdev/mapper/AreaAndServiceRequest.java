package com.microdev.param;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AreaAndServiceRequest {

    private String workerID;

    private Map<String,Integer> areaCode;


    private List<String> serviceType;
}