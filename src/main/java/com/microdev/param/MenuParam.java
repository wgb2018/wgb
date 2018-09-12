package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MenuParam {

    private String id;
    private String name;
    private Integer level;
    private Integer status;
    private Set<Integer> set = new HashSet<>();
}
