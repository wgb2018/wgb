package com.microdev.service;

import com.microdev.param.TokenDTO;
import com.microdev.param.UserDTO;


/**
 * @author liutf
 */
public interface TokenService {
    /**
     * 登录用户生成 token
     */
    TokenDTO accessToken(UserDTO user, String platform);

    /**
     * accessToken快过期的时候refreshToken
     */
    TokenDTO refreshToken(String refreshToken,String uniqueId) throws Exception;

    /**
     * 根据 accessToken 获取登录用户信息
     */
    UserDTO getUserByAccessToken(String accessToken);

    /**
     * 删除 token
     */
    void deleteAccessToken(String token);
}
