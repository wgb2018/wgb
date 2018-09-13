package com.microdev.param;

import lombok.Data;

@Data
public class DownLoadAccount {

    private String name;
    private String taskType;
    private String taskContent;
    private String workDate;
    private String StartEndTime;
    private double shouldPay;
    private double havePay;
    private double unConfirmedPay;
    private double paidPayMoney;
}
