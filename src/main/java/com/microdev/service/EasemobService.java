package com.microdev.service;

import io.swagger.client.ApiException;

/**
 * Created by easemob on 2017/3/16.
 */
public interface EasemobService {
    Object invokeEasemobAPI() throws ApiException;
}