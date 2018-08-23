package com.microdev.service.impl;

import com.microdev.common.im.InstanceMessageConfig;
import com.microdev.common.im.ResponseHandler;
import com.microdev.common.utils.HXTokenUtil;
import com.microdev.service.EasemobService;
import com.microdev.service.FileService;
import io.swagger.client.ApiException;
import io.swagger.client.api.UploadAndDownloadFilesApi;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileServiceImpl implements FileService {
    private ResponseHandler responseHandler = new ResponseHandler();
    private UploadAndDownloadFilesApi api = new UploadAndDownloadFilesApi();
    @Override
    public Object uploadFile(final Object file) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
                return api.orgNameAppNameChatfilesPost(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),(File)file,true);
             }
        });
    }

    @Override
    public Object downloadFile(final String fileUUID,final  String shareSecret,final Boolean isThumbnail) {
        return responseHandler.handle(new EasemobService() {
            @Override
            public Object invokeEasemobAPI() throws ApiException {
               return api.orgNameAppNameChatfilesUuidGet(InstanceMessageConfig.ORG_NAME, InstanceMessageConfig.APP_NAME, HXTokenUtil.getAccessToken(),fileUUID,shareSecret,isThumbnail);
            }
        });
    }
}
