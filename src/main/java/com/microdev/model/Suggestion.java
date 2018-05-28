package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("suggestion")
public class Suggestion extends BaseEntity {

    //意见内容
    private  String suggestionContent;

    private String userId;
}
