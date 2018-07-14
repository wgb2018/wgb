package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 用人单位、人力公司根据ID绑定
 */
@Data
public class HotelHrIdBindDTO {
    public HotelHrIdBindDTO(){
    }
    public HotelHrIdBindDTO(String hotelId, String hrId, Integer  bindType){
        this.hotelId=hotelId;
        this.hrId=hrId;
        this.bindType=bindType;
    }
    public HotelHrIdBindDTO(String hotelId, String hrId, Integer bindType, Integer relieveType){
        this.hotelId=hotelId;
        this.hrId=hrId;
        this.bindType=bindType;
        this.relieveType=relieveType;
    }
    /**
     * 用人单位
     */
    private String hotelId;
    /**
     * 人力公司信息
     */
    private String hrId;
    /**
     *   记录该关系是用人单位添加的人力公司；还是人力公司添加的用人单位
     * 1  用人单位添加的人力公司
     * 2：人力公司添加的用人单位
     */
    private Integer  bindType;
    private Integer  relieveType;
    private String   reason;
    private Set<String> set = new HashSet<>();

}
