package com.microdev.param;

import lombok.Data;

import java.util.Date;

@Data
public class HolidayDateInfo {

    private Date createTime;
    private Date modifyTime;
    private Date fromDate;
    private Date toDate;
}
