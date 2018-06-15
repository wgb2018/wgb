package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.type.UserType;
import lombok.Data;

@Data
@TableName("feedback")
public class FeedBack extends BaseEntity{
    private String userId;

    private String content;

    private UserType userType;
    @TableField(exist = false)
    private String userName;
}
