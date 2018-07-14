package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 用人单位支付人力公司的请求
 */
@Data
public class HotelPayHrRequest {
    /**
     * 任务Id
     */
    private String taskId;
    /**
     * 人力资源公司
     */
    private Set<HrPayDetailRequest> payHrSet= new HashSet<HrPayDetailRequest>();
}
