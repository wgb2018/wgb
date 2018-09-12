package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class PowerParam {

    private String id;
    private String name;
    private Integer code;
    private Integer level;
    private String identifer;
    private Set<String> set = new HashSet<>();
}
