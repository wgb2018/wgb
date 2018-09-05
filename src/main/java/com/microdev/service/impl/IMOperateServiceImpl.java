package com.microdev.service.impl;

import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.UserMapper;
import com.microdev.model.User;
import com.microdev.service.IMOperateService;
import com.microdev.service.IMUserService;
import io.swagger.client.model.Nickname;
import io.swagger.client.model.RegisterUsers;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 即时通讯
 */
@Service
public class IMOperateServiceImpl implements IMOperateService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IMUserService iMUserService;

    /**
     * 处理账号无法登陆即时通讯
     * @param id
     * @return
     */
    @Override
    public ResultDO checkAccountInfo(String id) {

        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数不能为空");
        }
        User user = userMapper.findByMobile(id);
        if (user == null) {
            throw new ParamsException("查询不到用户");
        }
        //判断用户是否注册IM用户
        Object obj = iMUserService.getIMUserByUserName(id);
        if (obj == null) {
            RegisterUsers registerUsers = new RegisterUsers();
            io.swagger.client.model.User u = new io.swagger.client.model.User();
            u.username(id).password(id);
            registerUsers.add(u);
            obj = iMUserService.createNewIMUserSingle(registerUsers);
        }
        if (obj == null) {
            return ResultDO.buildError("聊天通讯服务异常");
        }
        return ResultDO.buildSuccess("操作成功");
    }

    /**
     * 修改用户昵称
     * @param username      环信账号
     * @param nickName      昵称
     * @return
     */
    @Override
    public Object modifyUserNickName(String username, String nickName) {

        Nickname name = new Nickname().nickname(nickName);
        return iMUserService.modifyIMUserNickNameWithAdminToken(username, name);
    }
}
