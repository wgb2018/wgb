package com.microdev.service;

import com.microdev.common.ResultDO;

public interface IMOperateService {

    /**
     * 校验账号是否正确
     * @param id
     * @return
     */
    ResultDO checkAccountInfo(String id);

    /**
     * 修改用户昵称
     * @param username      环信账号
     * @param nickName      昵称
     * @return
     */
    Object modifyUserNickName(String username, String nickName);
}
