package com.microdev.param;

import com.microdev.model.UserArea;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AreaAndServiceRequest {

    private String workerID;

    private List<UserArea> areaCode;


    private List<String> serviceType;
}
