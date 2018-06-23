package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Version;
import com.microdev.param.VersionRequest;

public interface VersionService extends IService<Version> {
    ResultDO selectVersion(Paginator paginator, VersionRequest versionRequest);
}
