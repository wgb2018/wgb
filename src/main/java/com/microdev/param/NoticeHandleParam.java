package com.microdev.param;

import lombok.Data;

import java.util.List;

@Data
public class NoticeHandleParam {

    private List<NoticeHandle> param;

    //0 同意 1 拒绝
    private Integer status;


}
