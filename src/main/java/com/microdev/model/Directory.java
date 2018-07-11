package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("directory")
public class Directory extends BaseEntity{
    private String title;

    private Integer code;

    private String content;
}
