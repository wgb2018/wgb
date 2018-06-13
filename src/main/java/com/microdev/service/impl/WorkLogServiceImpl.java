package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.WorkerLogMapper;
import com.microdev.model.WorkLog;
import com.microdev.service.WorkLogService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class WorkLogServiceImpl extends ServiceImpl<WorkerLogMapper,WorkLog> implements WorkLogService {

}
