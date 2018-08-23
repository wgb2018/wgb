package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.ChatGroupService;
import com.microdev.service.EasemobService;
import io.swagger.client.ApiException;
import io.swagger.client.StringUtil;
import io.swagger.client.api.GroupsApi;
import io.swagger.client.model.*;
import org.springframework.stereotype.Service;

@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    private ResponseHandler responseHandler = new ResponseHandler();
    private GroupsApi api = new GroupsApi();
    @Override
    public Object getChatGroups(final Long limit,final String cursor) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,HXTokenUtil.getAccessToken(),limit+"",cursor);
            }
        });
    }

    @Override
    public Object getChatGroupDetails(final String[] groupIds) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdsGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,HXTokenUtil.getAccessToken(),StringUtil.join(groupIds,","));
            }
        });
    }
    @Override
    public Object createChatGroup(final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsPost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,HXTokenUtil.getAccessToken(), (Group) payload);
            }
        });
    }

    @Override
    public Object modifyChatGroup(final String groupId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdPut(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,HXTokenUtil.getAccessToken(),groupId, (ModifyGroup) payload);
            }
        });
    }

    @Override
    public Object deleteChatGroup(final String groupId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdDelete(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,HXTokenUtil.getAccessToken(),groupId);
            }
        });
    }

    @Override
    public Object getChatGroupUsers(final String groupId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdUsersGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId);
            }
        });
    }

    @Override
    public Object addSingleUserToChatGroup(final String groupId,final String userId) {
        final UserNames userNames = new UserNames();
        UserName userList = new UserName();
        userList.add(userId);
        userNames.usernames(userList);
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdUsersPost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,userNames);
            }
        });
    }

    @Override
    public Object addBatchUsersToChatGroup(final String groupId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdUsersPost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId, (UserNames) payload);
            }
        });
    }

    @Override
    public Object removeSingleUserFromChatGroup(final String groupId,final String userId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdUsersUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,userId);
            }
        });
    }

    @Override
    public Object removeBatchUsersFromChatGroup(final String groupId,final String[] userIds) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdUsersMembersDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,StringUtil.join(userIds,","));
            }
        });
    }

    @Override
    public Object transferChatGroupOwner(final String groupId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupidPut(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId, (NewOwner) payload);
            }
        });
    }

    @Override
    public Object getChatGroupBlockUsers(final String groupId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdBlocksUsersGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId);
            }
        });
    }

    @Override
    public Object addSingleBlockUserToChatGroup(final String groupId,final String userId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdBlocksUsersUsernamePost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,userId);
            }
        });
    }

    @Override
    public Object addBatchBlockUsersToChatGroup(final String groupId,final Object payload) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdBlocksUsersPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId, (UserNames) payload);
            }
        });
    }

    @Override
    public Object removeSingleBlockUserFromChatGroup(final String groupId,final String userId) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdBlocksUsersUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,userId);
            }
        });
    }

    @Override
    public Object removeBatchBlockUsersFromChatGroup(final String groupId,final String[] userIds) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatgroupsGroupIdBlocksUsersUsernamesDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),groupId,StringUtil.join(userIds,","));
            }
        });
    }
}
