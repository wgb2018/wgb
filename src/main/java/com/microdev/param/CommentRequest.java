package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CommentRequest {

    private String billId;
    private Set<String> evaluateId = new HashSet<>();
    private String comment;//评论
}
