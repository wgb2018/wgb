package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("inform_template")
public class InformTemplate extends BaseEntity {
    private String coede;

    private String content;

    private String msgLink;

    private String title;

}
