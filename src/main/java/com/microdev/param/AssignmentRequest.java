package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AssignmentRequest {

    private String id;
    private Set<String> set = new HashSet<>();
}
