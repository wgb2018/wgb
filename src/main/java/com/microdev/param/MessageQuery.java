package com.microdev.param;

import lombok.Data;

@Data
public class MessageQuery {
    /**
     * 小时工
     */
    private String workerId;
    /**
     * 用人单位
     */
    private String hotelId;
    /**
     * 人力资源公司
     */
    private String hrCompanyId;
    /**
     * 消息类型
     */
    private Integer applyType;

}
