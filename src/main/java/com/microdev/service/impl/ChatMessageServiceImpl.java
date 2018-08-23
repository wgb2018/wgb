package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.ChatMessageService;
import com.microdev.service.EasemobService;
import io.swagger.client.ApiException;
import io.swagger.client.api.ChatHistoryApi;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private ResponseHandler responseHandler = new ResponseHandler();
    private ChatHistoryApi api = new ChatHistoryApi();

    @Override
    public Object exportChatMessages(final Long limit,final String cursor,final String query) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatmessagesGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),query,limit+"",cursor);
            }
        });
    }

    @Override
    public Object exportChatMessages(final String timeStr) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatmessagesTimeGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),timeStr);
            }
        });
    }
}
