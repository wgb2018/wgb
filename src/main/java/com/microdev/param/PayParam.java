package com.microdev.param;

import lombok.Data;

@Data
public class PayParam {
    private String taskHrId;

    private String taskWorkerId;

    private Double payMoney;
}
