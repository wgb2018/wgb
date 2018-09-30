package com.microdev.common.utils;


import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.microdev.mapper.UserMapper;
import com.microdev.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import static cn.jpush.api.push.model.notification.PlatformNotification.ALERT;

public class JPushManage {

    @Autowired
    static UserMapper userMapper;
    public static PushPayload buildPushObject_all_all_alert() {
        return PushPayload.alertAll(ALERT);
    }
    public static PushPayload buildPushObject_all_alias_message(String alias,String notification)  {
       /* User u = userMapper.findByMobile (alias);
        if(u == null){
            System.out.println ("推送手机号不存在");
        }
        u.setMsNum (u.getMsNum ()+1);
        userMapper.updateById (u);*/
        if(notification == null){
            notification = "";
        }
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.newBuilder ()
                        .setAlert (notification)
                        .addPlatformNotification (IosNotification.newBuilder ()
                            //.setBadge (u.getMsNum ())
                            .setSound ("default")
                            .build ())
                        .build ())
                .setOptions (
                        Options.newBuilder()
                                .setApnsProduction(true)
                                .build()
                )
                .build();

    }
    public static PushPayload buildPushObject_all_message(String alias,String message)  {
       /* User u = userMapper.findByMobile (alias);
        if(u == null){
            System.out.println ("推送手机号不存在");
        }
        u.setMsNum (u.getMsNum ()+1);
        userMapper.updateById (u);*/
        if(message == null){
            message = "";
        }
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setMessage (Message.content (message))
                .build();
    }

}