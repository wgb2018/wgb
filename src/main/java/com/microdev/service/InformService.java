package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Inform;
import com.microdev.param.InformRequestDTO;
import com.microdev.param.InformType;

import java.util.Map;

public interface InformService extends IService<Inform> {
    void sendInform(String acceptype, String receiveTd, InformType informType,Map<String, String> templateParams);

    /**
     * 查询消息及未读消息
     * @return
     */
    int selectMessageInfo(InformRequestDTO dto, Paginator paginator);
}
