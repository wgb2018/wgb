package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 酒店、人力公司根据ID绑定
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
     * 酒店
     */
    private String hotelId;
    /**
     * 人力公司信息
     */
    private String hrId;
    /**
     *   记录该关系是酒店添加的人力公司；还是人力公司添加的酒店
     * 1  酒店添加的人力公司
     * 2：人力公司添加的酒店
     */
    private Integer  bindType;
    private Integer  relieveType;
    private Set<String> set = new HashSet<>();

}
