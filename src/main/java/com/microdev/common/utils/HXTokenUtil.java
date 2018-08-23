package com.microdev.common.utils;

import com.google.gson.Gson;
import com.microdev.common.im.InstanceMessageConfig;
import io.swagger.client.ApiException;
import io.swagger.client.api.AuthenticationApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HXTokenUtil {

    private static AuthenticationApi API = new AuthenticationApi();
    private static String ACCESS_TOKEN = null;
    private static Double EXPIREDAT = -1D;
    private static final Logger logger = LoggerFactory.getLogger(HXTokenUtil.class);

    public static void initTokenByProp() {
        String resp = null;
        try {
            resp = API.orgNameAppNameTokenPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, InstanceMessageConfig.getBODY());
        } catch (ApiException e) {
            logger.error(e.getMessage());
        }
        Gson gson = new Gson();
        Map map = gson.fromJson(resp, Map.class);
        ACCESS_TOKEN = "Bearer " + map.get("access_token");
        EXPIREDAT = System.currentTimeMillis() + (Double) map.get("expires_in") * 1000 - 1000;
    }

    public static String getAccessToken() {
        if (ACCESS_TOKEN == null || isExpired()) {
            initTokenByProp();
        }
        return ACCESS_TOKEN;
    }

    private static Boolean isExpired() {
        return System.currentTimeMillis() > EXPIREDAT;
    }
}
