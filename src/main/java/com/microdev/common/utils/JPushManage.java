package com.microdev.common.utils;


import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

import static cn.jpush.api.push.model.notification.PlatformNotification.ALERT;

public class JPushManage {


    public static PushPayload buildPushObject_all_all_alert() {
        return PushPayload.alertAll(ALERT);
    }
    public static PushPayload buildPushObject_all_alias_message(String alias,String notification)  {
        if(notification == null){
            notification = "";
        }
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.alert(notification))
                .build();
    }
}