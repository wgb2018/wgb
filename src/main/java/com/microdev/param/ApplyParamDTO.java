package com.microdev.param;

import lombok.Data;

@Data
public class ApplyParamDTO {

    private String id;//角色id
    private String roleType;//角色类型小时工worker用人单位hotel人力hr
    private String name;
    private String taskTypeText;
}
