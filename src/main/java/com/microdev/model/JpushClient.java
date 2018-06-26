package com.microdev.model;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import org.springframework.stereotype.Component;

@Component
public class JpushClient {
    public JPushClient jC = new JPushClient ("8a5e85f2ec95f9bcf0ac980c","6eb088f46e8e8bb874118f2d", null, ClientConfig.getInstance());
}
