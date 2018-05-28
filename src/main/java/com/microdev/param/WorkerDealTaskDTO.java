package com.microdev.param;

import lombok.Data;

@Data
public class WorkerDealTaskDTO {
    /**
     * 处理任务的小时工标识ID
     */
    private String workerId;
    /**
     * 指派该任务的人力公司标识ID
     */
    private String hrCompanyId;
    /**
     * 任务标识ID
     */
    private String taskId;
    /**
     * 是否接受任务
     */
    private boolean accepted;
    /**
     * (如果拒绝任务)拒绝原因
     */
    private String remark;
}
