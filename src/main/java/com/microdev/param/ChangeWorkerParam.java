package com.microdev.param;

import lombok.Data;

@Data
public class ChangeWorkerParam {
    private String taskWorkerId;

    private String reason;

    private String changeWorkerId;
}
