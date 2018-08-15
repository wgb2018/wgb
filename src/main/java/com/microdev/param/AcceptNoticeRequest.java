package com.microdev.param;

import lombok.Data;

@Data
public class AcceptNoticeRequest {
    private String noticeId;
    private String hrCompanyId;
    private String workerId;
    private Integer enrollWorkers;
}
