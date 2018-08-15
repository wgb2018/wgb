package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class QueryNoticeRequest {

    private String date;

    private String service;

    private String hotelName;

    private String hrCompanyName;

    private String hotelId;

    private OffsetDateTime fromDate;

    private OffsetDateTime toDate;

    private String hrId;

    private String workerId;
    //发布公告是 1 酒店发布 2 人力发布
    //查询公告报名情况是 1 小时工报名情况 2 人力公司报名情况
    private String type;
    //查询公告报名情况是 0 未处理 1 已接受 2 已拒绝
    private Integer status;

    private String noticeId;

    private boolean myself = true;
    // null 不排序 0 正序 1倒序
    private Integer sortByMoney;

}
