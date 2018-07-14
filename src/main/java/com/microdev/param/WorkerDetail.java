package com.microdev.param;

import lombok.Data;

import java.util.*;

@Data
public class WorkerDetail {

    private String time;//打卡日期
    private String expire;//是否过期0是1否
    private Map<String, Integer> hotelStatus = new HashMap<>();//用人单位显示状态
    private Map<String, Integer> sysStatus = new HashMap<>();//系统状态
    private List<PunchInfo> workList = new ArrayList<>();
    private int workHour;//工作时间（单位分钟)
    private double payment;//应付薪酬（单位元)
}
