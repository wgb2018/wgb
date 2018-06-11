package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MessageParamDTO {
    private String messgeId;
    private Set<String> set = new HashSet<>();
    private String reason;
}
