package com.microdev.common.utils;

import com.microdev.common.exception.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author liutf
 */
public class TokenUtil {
    /**
     * 从 request 请求中解析 token
     * post请求会把 token 放到 header 中
     * get 请求会把 token 放到 url 中
     */
    public static String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || bearerToken.equals("")) {
            bearerToken = request.getParameter("access_token");
        }

        if (bearerToken == null || bearerToken.equals("")) {
            throw new AuthenticationException("请求中缺少access_token");
        }

        if (bearerToken.contains(" ")) {//Bearer ff10450c1c897bfa01a26d82bdd57570d3421b13
            return bearerToken.split("\\s+")[1];
        } else {
            return bearerToken;
        }

    }

}
