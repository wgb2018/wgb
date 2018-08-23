package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.EasemobService;
import com.microdev.service.IMUserService;
import io.swagger.client.ApiException;
import io.swagger.client.api.UsersApi;
import io.swagger.client.model.NewPassword;
import io.swagger.client.model.Nickname;
import io.swagger.client.model.RegisterUsers;
import io.swagger.client.model.UserNames;
import org.springframework.stereotype.Service;

@Service
public class IMUsersServiceImpl implements IMUserService {

	private UsersApi api = new UsersApi();
	private ResponseHandler responseHandler = new ResponseHandler();
	@Override
	public Object createNewIMUserSingle(final Object payload) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, (RegisterUsers) payload, HXTokenUtil.getAccessToken());
			}
		});
	}

	@Override
	public Object createNewIMUserBatch(final Object payload) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, (RegisterUsers) payload, HXTokenUtil.getAccessToken());
			}
		});
	}

	@Override
	public Object getIMUserByUserName(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
		}
		});
	}

	@Override
	public Object getIMUsersBatch(final Long limit,final String cursor) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),limit+"",cursor);
			}
		});
	}

	@Override
	public Object deleteIMUserByUserName(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object deleteIMUserBatch(final Long limit,final String cursor) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),limit+"",cursor);
			}
		});
	}

	@Override
	public Object modifyIMUserPasswordWithAdminToken(final String userName, final Object payload) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernamePasswordPut(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME,userName, (NewPassword) payload, HXTokenUtil.getAccessToken());
			}
		});
	}

	@Override
	public Object modifyIMUserNickNameWithAdminToken(final String userName,final Object payload) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernamePut(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME,userName, (Nickname) payload, HXTokenUtil.getAccessToken());
			}
		});
	}

	@Override
	public Object addFriendSingle(final String userName,final String friendName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameContactsUsersFriendUsernamePost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName,friendName);
			}
		});
	}

	@Override
	public Object deleteFriendSingle(final String userName,final String friendName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameContactsUsersFriendUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName,friendName);
			}
		});
	}

	@Override
	public Object getFriends(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameContactsUsersGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object getBlackList(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameBlocksUsersGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object addToBlackList(final String userName,final Object payload) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameBlocksUsersPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName, (UserNames) payload);
			}
		});
	}

	@Override
	public Object removeFromBlackList(final String userName,final String blackListName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameBlocksUsersBlockedUsernameDelete(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName,blackListName);
			}
		});
	}

	@Override
	public Object getIMUserStatus(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameStatusGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object getOfflineMsgCount(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersOwnerUsernameOfflineMsgCountGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object getSpecifiedOfflineMsgStatus(final String userName,final String msgId) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameOfflineMsgStatusMsgIdGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName,msgId);
			}
		});
	}

	@Override
	public Object deactivateIMUser(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameDeactivatePost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object activateIMUser(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameActivatePost(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object disconnectIMUser(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameDisconnectGet(InstanceMessageConfig.ORG_NAME,InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object getIMUserAllChatGroups(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameJoinedChatgroupsGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}

	@Override
	public Object getIMUserAllChatRooms(final String userName) {
		return responseHandler.handle(new EasemobService() {
			@Override
			public Object invokeEasemobAPI() throws ApiException {
				return api.orgNameAppNameUsersUsernameJoinedChatroomsGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),userName);
			}
		});
	}
}
