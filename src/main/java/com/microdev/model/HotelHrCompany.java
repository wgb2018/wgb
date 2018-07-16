package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 公司
 *
 * @author yinbaoxin 重新定义用人单位和人力公司的关系
 * 加入 绑定关系和绑定时间等
 */
@Data
@TableName("hotel_hr_company")
public class HotelHrCompany extends BaseEntity {

    /**
     * 绑定时间
     */
    private OffsetDateTime bindTime;
    /**
     *   记录该关系是用人单位添加的人力公司；还是人力公司添加的用人单位
     * 1  用人单位添加的人力公司
     * 2：人力公司添加的用人单位
     */
    private Integer  bindType;
    /**
     * 解除关系时间
     */
    private OffsetDateTime relieveTime;
    /**
     *
     * 1  用人单位移除的人力公司
     * 2：人力移除的用人单位
     */
    private Integer  relieveType;
    /**
     *     当前状态
     * 0   合作中
     * 1   合作过
     * 3   待审核
     * 4   拒绝
     * 5   解绑中
     */
    private Integer status;

    /**
     * 用人单位信息
     */
    private String hotelId;
    /**
     * 人力公司信息
     */
    private String hrId;
    /**
     * 绑定协议
     */
    private String bindProtocol;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
