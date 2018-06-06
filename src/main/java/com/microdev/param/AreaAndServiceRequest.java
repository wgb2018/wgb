package com.microdev.param;

import com.microdev.model.UserArea;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AreaAndServiceRequest {

    private String id;

    private List<UserArea> areaCode;


    private List<String> serviceType;

    private Integer idType;

    private List<String> areaCodeList;
}
