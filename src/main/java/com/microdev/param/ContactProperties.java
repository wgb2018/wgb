package com.microdev.param;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author liutf
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.contact")
public class ContactProperties {

    private SmsProperties sms = new SmsProperties();

    @Data
    public static class SmsProperties {
        String accessKeyId;
        String accessKeySecret;
        Integer defaultConnectTimeout;
        Integer defaultReadTimeout;
        /**
         * 短信验证码长度，默认6位
         */
        Integer smsCodeLength = 6;
    }
}
