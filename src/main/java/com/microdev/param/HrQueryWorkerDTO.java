package com.microdev.param;

import lombok.Data;

@Data
public class HrQueryWorkerDTO {

    private String hrId;

    private String hotelId;

    private String userName;

    private String taskId;
//     * 0   未审核
//     * 1   通过
//     * 2   拒绝
//     * 3   解绑中
//     * 4   已解绑
    private Integer status;
}
