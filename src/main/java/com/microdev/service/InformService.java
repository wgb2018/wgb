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
    Map<String, Object> selectMessageInfo(InformRequestDTO dto, Paginator paginator);

    /**
     * 发送通知
     * @param sendType      发送类型1小时工2人力3酒店4系统
     * @param acceptType    接收类型1小时工2人力3酒店
     * @param content       通知内容
     * @param receiveId     接收方id
     * @param title         消息标题
     */
    void sendInformInfo(int sendType, int acceptType, String content, String receiveId, String title);
}
