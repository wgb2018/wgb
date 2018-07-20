package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("serviceComment")
public class ServiceComment extends BaseEntity{

    /**
     * 评论看法
     */
    private String comment;
    /**
     * 付款id
     */
    private String billId;
    /**
     * 标签id
     */
    private String evaluateId;
}
