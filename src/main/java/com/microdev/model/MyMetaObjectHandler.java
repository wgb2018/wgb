package com.microdev.model;

import com.baomidou.mybatisplus.mapper.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class MyMetaObjectHandler extends MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", OffsetDateTime.now());
        metaObject.setValue("modifyTime", OffsetDateTime.now());
        System.out.println("------------++++++");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("modifyTime", OffsetDateTime.now());
    }
}
