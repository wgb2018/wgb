package com.microdev.param;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 人力公司支付小时工的请求
 */
@Data
public class HrPayWorkerRequest {
    /**
     * 任务Id
     */
    private String hrTaskId;
    /**
     *小时工信息
     */
    private Set<WorkerPayDetailRequest> payWorkerSet= new HashSet<WorkerPayDetailRequest>();
}
