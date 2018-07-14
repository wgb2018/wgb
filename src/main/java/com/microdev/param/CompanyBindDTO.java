package com.microdev.param;

import lombok.Data;

/**
 * 用人单位和人力资源公司绑定的DTO
 */
@Data
public class CompanyBindDTO {
    /**
     * 用人单位/人力资源公司ID
     */
    private String id;
    /**
     * 公司名称
     */
    private String name;
    /**
     * 负责人
     */
    private String leader;
    /**
     * 负责人电话
     */
    private String leaderMobile;

}
