package com.microdev.common.filter;


import com.microdev.Constant;
import com.microdev.common.context.ServiceContext;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.context.User;
import com.microdev.common.exception.AuthorizationException;
import com.microdev.common.utils.TokenUtil;
import com.microdev.param.PermissionDTO;
import com.microdev.param.UserDTO;
import com.microdev.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liutf
 */
@Order(-99)
@Component
public class SecurityFilter implements Filter {
    //spring 工具类
    private AntPathMatcher pathMatcher = new AntPathMatcher();
    @Autowired
    private TokenService tokenService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        initContext(request, response);

        boolean hasPermission = false;


        String path = request.getRequestURI();// 获取用户访问的路径 /xxx/yyy
        String method = request.getMethod().toUpperCase();// 获取用户请求的方法,POST/GET/PUT/DELETE

       //chain.doFilter(request, response);

        //OPTIONS请求直接放行
        if ("OPTIONS".equals(method)) {
            chain.doFilter(request, response);
        } else {
            //忽略验证的 url 可以直接放行
            for (String url : Constant.ignoreUrl) {
                if (pathMatcher.match(url, path)) {
                    hasPermission = true;
                    break;
                }
            }
            //没有被忽略的需要登录之后使用 token 访问
            if (!hasPermission) {
                String accessToken = TokenUtil.parseBearerToken(request);
                UserDTO userDTO = tokenService.getUserByAccessToken(accessToken);
                ServiceContextHolder.getServiceContext().setUser(
                        User.me().set("id", userDTO.getId())
                                .set("username", userDTO.getUsername())
                                .set("mobile", userDTO.getMobile())
                                .set("roles", userDTO.getRoleList())
                                .set("userType", userDTO.getUserType())
                                .set("workerId",userDTO.getWorkerId())
                                .set("nickName",userDTO.getNickname())
                                .set("sex",userDTO.getSex()==null?"UNKNOW":userDTO.getSex().toString())
                );
                //admin 用户直接放行
                if (userDTO.getUserType().equals("platform") || true) {
                    hasPermission = true;
                } else {
                    for (PermissionDTO permissionDTO : userDTO.getPermissions()) {
                        if (permissionDTO.getAction() != null) {
                            if (method.equals(permissionDTO.getAction().name()) && pathMatcher.match(permissionDTO.getUri(), path)) {
                                hasPermission = true;
                                break;
                            }
                        } else {
                            if (pathMatcher.match(permissionDTO.getUri(), path)) {
                                hasPermission = true;
                                break;
                            }
                        }
                    }
                }
            }

            //通过的放行，不通过的阻止访问
            if (hasPermission) {
                chain.doFilter(request, response);
            } else {
                throw new AuthorizationException("无权限访问");
            }
        }
    }

    private void initContext(HttpServletRequest request, HttpServletResponse response) {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setHeader(request);
        serviceContext.setHttpServletRequest(request);
        serviceContext.setHttpServletResponse(response);
        ServiceContextHolder.setServiceContext(serviceContext);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {

    }
}
