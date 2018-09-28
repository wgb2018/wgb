package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.User;
import com.microdev.param.*;

import java.util.List;
import java.util.Map;


public interface UserService extends IService<User>{
       User create(User user) throws Exception;

       //子帐号注册
       ResultDO registerchild(accountParam userDTO) throws Exception;

       List<User> query(UserDTO user) throws Exception;
       //用户名+密码登录
       ResultDO login(UserDTO user) throws Exception;

       //用户注册
       ResultDO register(UserDTO userDTO) throws Exception;
       /**
        * 退出登录
        */
       ResultDO logout(String mobile);
       /**
        * 修改密码
        */
       TokenDTO changePwd(ChangePwdRequest request ) throws Exception;
       /**
        * 手机号+短信验证码 登录
        */
       TokenDTO smslogin(UserDTO login);
       /**
        * 微信小程序用户注册并登录
        */
       TokenDTO register_weixin(WeixinUserInfo weixinUserInfo) throws Exception;
       /**
        * 重置密码
        */
       void resetPwd(UserDTO user) throws Exception;
       /**
        * 修改基础信息
        */
       void modifyBaseInfo(UserDTO userDTO);
       /**
        * 修改手机号
        *
        * @param newMobile        新手机号
        * @param newMobileSmsCode 新手机号短信验证码
        */
       void modifyMobile(String newMobile, String newMobileSmsCode);
       /**
        * 个人信息
        *
        * @return
        */
       UserDTO me();

       ResultDO feedbackQuery(Paginator paginator, FeedbackQueryDTO feedbackQueryDTO);

       ResultDO feedbackInsert(FeedBackParam request) throws Exception;

}
