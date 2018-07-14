package com.microdev.model;


import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.type.UserType;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 *
 * @author yinbaoxin 定义人员（小时工、或员工）和公司的关系
 */
@Data
@TableName("user_company")
public class UserCompany extends BaseEntity {

    /**
     * 绑定时间
     */
    private OffsetDateTime confirmedDate;
    /**
     *   当前状态
     * 0   未审核
     * 1   已绑定
     * 2   拒绝绑定（）
     * 3   解绑中
     * 4   已解绑
     */
    private Integer status;
    /**
     * ybx 拒绝理由
     */
    private String refusedReason;
    /**
     * 解除关系时间
     */
    private OffsetDateTime relieveTime;
    /**
     * 已确定接单数
     */
    private Integer allTasks=0;
    /**
     * 已确定接单数
     */
    private Integer confirmedTasks=0;
    /**
     * 已拒绝接单数
     */
    private Integer refusedTasks=0;
    /**
     * 违约单数
     */
    private Integer noPromiseTasks=0;

    /**
     * 人员信息
     */
    private String userId;

	@TableField(exist = false)
    private User user;
    private UserType userType;

    /**
     * 公司信息
     */
    private String companyId;
    /**
     * 公司类型，1：用人单位，2：人力公司
     */
    private Integer companyType;
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
