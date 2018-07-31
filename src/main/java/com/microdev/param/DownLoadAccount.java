package com.microdev.param;

import lombok.Data;

@Data
public class DownLoadAccount {

    private String taskType;
    private String taskContent;
    private String workDate;
    private String StartEndTime;
    private String name;
    private double shouldPay;
    private double havePay;
    private double paidPayMoney;
    private double unConfirmedPay;
}
