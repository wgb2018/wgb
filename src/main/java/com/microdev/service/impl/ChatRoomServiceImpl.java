package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.ChatRoomService;
import com.microdev.service.EasemobService;
import io.swagger.client.ApiException;
import io.swagger.client.StringUtil;
import io.swagger.client.api.ChatRoomsApi;
import io.swagger.client.model.Chatroom;
import io.swagger.client.model.ModifyChatroom;
import io.swagger.client.model.UserNames;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    private ResponseHandler responseHandler = new ResponseHandler();
    private ChatRoomsApi api = new ChatRoomsApi();

    @Override
    public Object createChatRoom(final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(), (Chatroom) payload);
            }
        });
    }

    @Override
    public Object modifyChatRoom(final String roomId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdPut(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId, (ModifyChatroom) payload);
            }
        });
    }

    @Override
    public Object deleteChatRoom(final String roomId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdDelete(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId);
            }
        });
    }

    @Override
    public Object getAllChatRooms() {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken());
            }
        });
    }

    @Override
    public Object getChatRoomDetail(final String roomId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId);
            }
        });
    }

    @Override
    public Object addSingleUserToChatRoom(final String roomId,final String userName) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdUsersUsernamePost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId,userName);
            }
        });
    }

    @Override
    public Object addBatchUsersToChatRoom(final String roomId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdUsersPost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId, (UserNames) payload);
            }
        });
    }

    @Override
    public Object removeSingleUserFromChatRoom(final String roomId,final String userName) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdUsersUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId,userName);
            }
        });
    }

    @Override
    public Object removeBatchUsersFromChatRoom(final String roomId,final String[] userNames) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatroomsChatroomIdUsersUsernamesDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),roomId, StringUtil.join(userNames,","));
            }
        });
    }
}
