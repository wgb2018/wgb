package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.model.WorkLog;

public interface WorkLogService extends IService<WorkLog> {

    /**
     * 更新查看标识
     * @param taskWorkerId
     * @param date
     * @return
     */
    String updateCheckSign(String taskWorkerId, String date);
}
