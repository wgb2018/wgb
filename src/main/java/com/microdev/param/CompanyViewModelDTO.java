package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author 尹保新 ，查询数据返回界面的对象
 */
@Data
public class CompanyViewModelDTO extends CompanyDTO {
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
     * 创建时间
     */
    private OffsetDateTime createTime;
    /**
     * 最后一次更新时间
     */
    private OffsetDateTime modifyTime;

    /**
     * 逻辑删除  默认：false，true为删除
     */
    private Boolean deleted;


    //用人单位人力公司的绑定状态
    private Integer bindStatus;

}
