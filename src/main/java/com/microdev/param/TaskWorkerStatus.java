package com.microdev.param;

/**
 * 小时工任务状态
 */
public enum TaskWorkerStatus {
    /**
     * 未处理的初始状态
     */
    UNTREATED,
    /**
     * 接受任务
     */
    ACCEPTED,
    /**
     * 拒绝任务
     */
    REFUSED,
    /**
     * 进行中
     */
    WORKING,
    /**
     * 结束
     */
    FINISHED,
}
