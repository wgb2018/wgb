package com.microdev.param;

import lombok.Data;

/**
 * @author liutf
 */
@Data
public class TokenDTO {
    /**
     * 表示访问令牌，必选项。
     */
    private String access_token;
    /**
     * 表示令牌类型，该值大小写不敏感，必选项，可以是bearer类型或mac类型。
     */
    private String token_type;
    /**
     * 表示过期时间，单位为秒。如果省略该参数，必须其他方式设置过期时间。
     */
    private Long expires_in;
    /**
     * 表示更新令牌 ， 用来获取下一次的访问令牌 ， 可选项 。
     */
    private String refresh_token;
    /**
     * 表示权限范围 ， 如果与客户端申请的范围一致 ， 此项可省略 。
     */
    private String scope;

    /**
     * 微信联合登录的openid
     */
    private String openid;

}
