package com.microdev.param;

import lombok.Data;

@Data
public class InformRequestDTO {

    //小时工传workerId酒店人力传id
    private String id;
    //用户角色worker小时工hotel酒店hr人力
    private String role;
    //1小时工2人力公司3酒店4系统
    private int type;
}
