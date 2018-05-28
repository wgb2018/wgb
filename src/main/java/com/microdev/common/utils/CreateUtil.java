package com.microdev.common.utils;

import com.microdev.model.BaseEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateUtil {
    public static Object create(Object t){
        BaseEntity obj = (BaseEntity)t;
        obj.setPid(UUID.randomUUID().toString());
        obj.setCreateTime(OffsetDateTime.now());
        return obj;
    }
}
