package com.microdev.service;

import com.microdev.common.ResultDO;

public interface IMOperateService {

    /**
     * 校验账号是否正确
     * @param id
     * @return
     */
    ResultDO checkAccountInfo(String id);
}
