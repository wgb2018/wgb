package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.EasemobService;
import com.microdev.service.SendMessageService;
import io.swagger.client.ApiException;
import io.swagger.client.api.MessagesApi;
import io.swagger.client.model.Msg;
import org.springframework.stereotype.Service;

@Service
public class SendMessageServiceImpl implements SendMessageService {
    private ResponseHandler responseHandler = new ResponseHandler();
    private MessagesApi api = new MessagesApi();
    @Override
    public Object sendMessage(final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameMessagesPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(), (Msg) payload);
            }
        });
    }
}
