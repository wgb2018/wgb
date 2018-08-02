package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkerBindCompany {

    private OffsetDateTime createTime;
    private OffsetDateTime confirmedDate;
    private OffsetDateTime modifyTime;
    //绑定状态
    private Integer status;
    private String name;
    private String logo;
    private String leader;
    private String leaderMobile;
    private String address;
    private String area;
    private Integer companyStatus;
    //当companyStatus=3申请解绑中时，统计距离解绑有XX天XX小时
    private String hour;
    private String pid;
    private String bindProtocol;
    private String businessLicense;
    private String laborDispatchCard;
}
