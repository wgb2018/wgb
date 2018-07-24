package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
public class EvaluateCommentRelation {

    private String serviceCommentId;
    private String evaluateId;
}
