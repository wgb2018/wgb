package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.microdev.common.exception.AuthenticationException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.utils.JPushManage;
import com.microdev.mapper.TaskWorkerMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.mapper.VersionMapper;
import com.microdev.model.JpushClient;
import com.microdev.model.User;
import com.microdev.param.TokenDTO;
import com.microdev.param.TokenProperties;
import com.microdev.param.UserDTO;
import com.microdev.service.TokenService;
import com.microdev.type.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author liutf
 */
@Component
@ConditionalOnProperty(prefix = "custom.security.oauth.token", name = "storeType", havingValue = "jwt", matchIfMissing = true)
public class JwtTokenServiceImpl implements TokenService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TokenProperties tokenProperties;
    @Autowired
    private VersionMapper versionMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JpushClient jpushClient;

    /**
     * 用户登录成功，获取token
     */
    @Override
    public TokenDTO accessToken(UserDTO user, String platform) {

        String userId = user.getId();
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getNickname());
        map.put("mobile", user.getMobile());
        map.put("roles", user.getRoleList());
        map.put("userType", user.getUserType());
        map.put("platform", platform);


        OffsetDateTime nowDateTime = OffsetDateTime.now();
        OffsetDateTime access_token_date = nowDateTime.plusSeconds(tokenProperties.getAccessTokenLifetimeSeconds());
        OffsetDateTime refresh_token_date = nowDateTime.plusSeconds(tokenProperties.getRefreshTokenLifetimeSeconds());

        String access_token = jwtBuilder(map, userId, nowDateTime, access_token_date);
        String refresh_token = jwtBuilder(map, userId, nowDateTime, refresh_token_date);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setOpenid(user.getOpenId());
        tokenDTO.setAccess_token(access_token);
        tokenDTO.setRefresh_token(refresh_token);
        tokenDTO.setToken_type("bearer");
        tokenDTO.setExpires_in(tokenProperties.getAccessTokenLifetimeSeconds());
        return tokenDTO;
    }


    @Override
    public TokenDTO refreshToken(String refreshToken,String uniqueId) throws Exception{
        Claims body = jwtParser(refreshToken);
        //判断是否已经过期
        OffsetDateTime expiration = OffsetDateTime.ofInstant(Instant.ofEpochMilli(body.getExpiration().getTime()), ZoneId.systemDefault());
        if (OffsetDateTime.now().isAfter(expiration)) {
            throw new AuthenticationException("refresh_token已过期");
        }
        //没有过期就获取内容
        String userId = body.getSubject();
        User user = userMapper.queryByUserId(userId);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        //user.getAllRoleCodes();

        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getNickname());
        map.put("mobile", user.getMobile());
        map.put("userType", user.getUserType());
        map.put("platform", body.get("platform"));
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String value = operations.get (user.getMobile());
        if(value != null && !value.equals (uniqueId)){
            if(user.getUserType() == UserType.worker){
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_message (user.getMobile(),uniqueId));
                    System.out.println ("发送推送1");
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {
                    e.printStackTrace ( );
                }
            }
        }
        //重新生成 token
        OffsetDateTime nowDateTime = OffsetDateTime.now();
        OffsetDateTime access_token_date = nowDateTime.plusSeconds(tokenProperties.getAccessTokenLifetimeSeconds());
        String access_token = jwtBuilder(map, user.getPid(), nowDateTime, access_token_date);

        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccess_token(access_token);
        tokenDTO.setRefresh_token(refreshToken);
        tokenDTO.setToken_type("bearer");
        tokenDTO.setExpires_in(tokenProperties.getAccessTokenLifetimeSeconds());
        operations.set(user.getMobile (), uniqueId);
        return tokenDTO;
    }

    /**
     * 根据token获取用户信息
     *
     * @param accessToken 必填，tokenId
     */
    @Override
    public UserDTO getUserByAccessToken(String accessToken) {
        Claims body = jwtParser(accessToken);
        //判断是否已经过期
        OffsetDateTime expiration = OffsetDateTime.ofInstant(Instant.ofEpochMilli(body.getExpiration().getTime()), ZoneId.systemDefault());
        if (OffsetDateTime.now().isAfter(expiration)) {
            throw new AuthenticationException("access_token已过期");
        }
        //没有过期就获取内容
        String userId = body.getSubject();
        User user = userMapper.queryByUserId(userId);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getPid());
        userDTO.setUsername(user.getUsername());
        userDTO.setMobile(user.getMobile());
        userDTO.setUserType(user.getUserType());
        userDTO.setNickname(user.getNickname());
        userDTO.setSex(user.getSex());
        if (user.getUserType() == UserType.worker) {
            userDTO.setWorkerId(user.getWorkerId());
        } else {
            userDTO.setWorkerId("");
        }
        return userDTO;
    }


    /**
     * 清除AccessToken
     *
     * @param token
     */
    @Override
    public void deleteAccessToken(final String token) {
    }


    private Claims jwtParser(String token) {
        Claims body;
        try {
            body = Jwts.parser().setSigningKey(tokenProperties.getJwtSecret().getBytes("UTF-8")).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new ParamsException("验签失败", e);
        }
        return body;
    }

    private String jwtBuilder(Map<String, Object> map, String userId, OffsetDateTime issuedAt, OffsetDateTime expiration) {
        try {
            return Jwts.builder()
                    .setSubject(userId)
                    .setId(UUID.randomUUID().toString())
                    .setIssuedAt(Date.from(Instant.ofEpochSecond(issuedAt.toEpochSecond())))
                    .setExpiration(Date.from(Instant.ofEpochSecond(expiration.toEpochSecond())))
                    .addClaims(map)
                    .signWith(SignatureAlgorithm.HS512, tokenProperties.getJwtSecret().getBytes("UTF-8"))
                    .compact();
        } catch (Exception e) {
            throw new ParamsException("access_token生成失败", e.getMessage());
        }
    }
}
