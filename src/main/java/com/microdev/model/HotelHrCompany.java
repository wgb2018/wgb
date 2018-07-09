package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 公司
 *
 * @author yinbaoxin 重新定义酒店和人力公司的关系
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
     *   记录该关系是酒店添加的人力公司；还是人力公司添加的酒店
     * 1  酒店添加的人力公司
     * 2：人力公司添加的酒店
     */
    private Integer  bindType;
    /**
     * 解除关系时间
     */
    private OffsetDateTime relieveTime;
    /**
     *
     * 1  酒店移除的人力公司
     * 2：人力移除的酒店
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
     * 酒店信息
     */
    private String hotelId;
    /**
     * 人力公司信息
     */
    private String hrId;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
