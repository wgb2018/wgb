package com.microdev.param;

import lombok.Data;

/**
 * @author yinbaoxin
 * 拒绝任务的请求
 */
@Data
public class RefusedTaskRequest {

    /**
     * 小时工Id
     */
    private String workerId;
    /**
     * 小时工任务Id
     */
    private String workerTaskId;
    /**
     * 拒绝理由
     */
    private String refusedReason;
    /**
     * 消息id
     */
    private String messageId;

}
