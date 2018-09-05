package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公司
 *
 * @author liutf
 */
@Data
@TableName("company")
public class Company extends BaseEntity {
    @TableField(exist = false)
    private String userId;
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
     * 当前状态
     * 0    未审核
     * 1    已核准
     * 2    已冻结
     * -1   已注销
     */
    private Integer status;
    /**
     * 审核时间
     */
    private OffsetDateTime confirmedTime;
    /**
     * 纬度，范围为90 ~ -90
     */
    private Double latitude = 0.0;
    /**
     *  经度，范围为180 ~ -180。
     */
    private Double longitude = 0.0;
    /**
     *  活跃人数
     */
    private Integer activeWorkers;
    /**
     *  活跃人数
     */
    private Integer activeCompanys;

    private boolean bindCompanys = true;

    private boolean bindWorkers = true;

	@TableField(exist = false)
    private List <UserArea> areaCode;

    @TableField(exist = false)
    private List<Dict> serviceType;

    private Integer addressCode;

    private String area;
    /**
     * 用户二维码
     */
    private String qrCode;
    /**
     * 劳务派遣证
     */
    private String laborDispatchCard;
    //评分
    @TableField(exist = false)
    private String grade;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String pollCode;

}
