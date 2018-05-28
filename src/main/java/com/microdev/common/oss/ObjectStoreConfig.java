package com.microdev.common.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Created by Liutengfei on 2018/2/9
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.objectstore")
public class ObjectStoreConfig {
    private String platform;
    private Map<String, String> settings;
}
