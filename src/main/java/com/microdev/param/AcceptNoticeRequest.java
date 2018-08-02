package com.microdev.param;

import lombok.Data;

@Data
public class AcceptNoticeRequest {
    private String noticeId;
    private String HrCompanyId;
    private String workerId;
    private Integer enrollWorkers;
}
