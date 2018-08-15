package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("enroll")
public class Enroll extends BaseEntity{
    private Integer enrollType;

    private String enrollCode;

    private String enrollContent;

    private String enrollLink;

    private String enrollTitle;

    private String msgLink;

    //0 未处理 1 已同意 2 已拒绝
    private Integer status;

    private String workerId;

    private String workerTaskId;

    private String hotelId;

    private String hrCompanyId;

    private Integer applicantType;

    private Integer applyType;

    private String content;

    private Integer value;

    private String requestId;

    private Integer isTask;

    private String taskId;

    private String hrTaskId;
    //指派人数
    private Integer assign;
}
