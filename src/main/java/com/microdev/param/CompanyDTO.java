package com.microdev.param;

import lombok.Data;

/**
 * @author 创建和修改用人单位的数据对象
 */
@Data
public class CompanyDTO {
    private String id;
    /**
     * 公司名称
     */
    private String name;
    /**
     * 公司类型，1：用人单位，2：人力公司
     */
    private Integer companyType;
    /**
     * 公司 logo
     */
    private String logo;
    /**
     * 公司地址
     */
    private String address;
    /**
     * 营业执照
     */
    private String businessLicense;
    /**
     * 负责人
     */
    private String leader;
    /**
     * 负责人电话
     */
    private String leaderMobile;
    /**
     * 纬度，范围为90 ~ -90
     */
    private Double latitude;
    /**
     *  经度，范围为180 ~ -180。
     */
    private Double longitude;

    private Integer status;

    private String laborDispatchCard;

    private Integer addressCode;

    private String area;

    private String grade;




}
