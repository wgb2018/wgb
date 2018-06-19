package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("version")
public class Version extends BaseEntity{
    private String versionCode;

    private String content;

    private String bug;

    private String change;

    private String address;
}
