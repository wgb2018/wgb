package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

@Data
public class IMUserRelation {

    @TableId(value ="id",type = IdType.UUID)
    private String pid;
    private String userId;
    private String chatUserId;
    private Boolean deleted = false;
}
