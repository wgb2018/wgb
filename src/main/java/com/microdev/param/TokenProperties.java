package com.microdev.param;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author liutf
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.security.oauth.token")
public class TokenProperties {
    private String storeType;
    private String jwtSecret;
    private Long accessTokenLifetimeSeconds;
    private Long refreshTokenLifetimeSeconds;
}
