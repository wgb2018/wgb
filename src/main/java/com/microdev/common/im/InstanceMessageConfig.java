package com.microdev.common.im;

import io.swagger.client.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstanceMessageConfig {


    public static String GRANT_TYPE;

    public static String ORG_NAME;

    public static String APP_NAME;

    public static String CLIENT_ID;

    public static String CLIENT_SECRET;
    private static Token BODY;

    @Value("${grantType}")
    public void setGrantType(String grantType) {
        GRANT_TYPE = grantType;
    }

    @Value("${orgName}")
    public void setOrgName(String orgName) {
        ORG_NAME = orgName;
    }

    @Value("${appName}")
    public void setAppName(String appName) {
        APP_NAME = appName;
    }

    @Value("${clientId}")
    public void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }

    @Value("${clientSecret}")
    public void setClientSecret(String clientSecret) {
        CLIENT_SECRET = clientSecret;
    }

    public static Token getBODY() {
        if (BODY == null) {
            BODY = new Token().clientId(CLIENT_ID).grantType(GRANT_TYPE).clientSecret(CLIENT_SECRET);
        }
        return BODY;
    }

}
