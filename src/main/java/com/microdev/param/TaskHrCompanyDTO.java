package com.microdev.param;

import lombok.Data;

@Data
public class TaskHrCompanyDTO {
    /**
     * 人力资源公司
     */
    private String hrCompanyId;
    /**
     * 需要的人数
     */
    private Integer needWorkers;

}
