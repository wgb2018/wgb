package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.type.SocialType;
import lombok.Data;


/**
 * @author liutf
 */
@Data
@TableName("social")
public class Social extends BaseEntity {

    private SocialType type;

    private String opendId;

    private String unionid;

    private String sessionKey;

    private String userId;
    /**
     * 等级
     */
    private String level;
    /**
     * 昵称
     */
    private String displayName;
    /**
     * 性别
     */
    private String sex;
    /**
     * 语言
     */
    private String language;
    /**
     * 普通用户个人资料填写的省份
     */
    private String province;
    /**
     * 普通用户个人资料填写的城市
     */
    private String city;
    /**
     * 国家，如中国为CN
     */
    private String country;

    /**
     * 主页地址
     */
    private String profileUrl;
    /**
     * 头像地址
     */
    private String imageUrl;
}
