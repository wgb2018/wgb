package com.microdev.param;

import lombok.Data;

@Data
public class PayRecord {

    private String payer;
    private String cashier;
    private double salay;
    private String time;
    private String status;//0 未确认1 同意2 拒绝
}
