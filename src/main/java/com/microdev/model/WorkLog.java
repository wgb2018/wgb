package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("work_log")
public class WorkLog extends BaseEntity {

    /**
     * 小时工任务标识ID
     */
    private String taskWorkerId;

    /**
     * 任务标识ID
     */
    private String taskId;

    /**
     * 开始时间
     */
    private OffsetDateTime fromDate;

    /**
     * 结束时间
     */
    private OffsetDateTime toDate;

    /**
     * 从fromdate到todate的累计分钟数
     */
    private Integer minutes;

    /**
     * 就餐次数
     */
    private Integer repastTimes;

    /**
     * 打卡时间
     */
    private OffsetDateTime punchDate;

    /**
     * 1迟到
     * 2早退
     * 3旷工
     * 4忘打卡
     * 5迟到早退
     * 6早退忘打卡
     * 7迟到忘打卡
     * 8请假
     * 9请假旷工
     * 10请假早退
     * 11请假迟到早退
     * 12请假迟到
     */
    private Integer status;

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

}
