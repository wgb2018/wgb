package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Inform;
import com.microdev.param.InformRequestDTO;
import com.microdev.param.InformType;

import java.util.List;
import java.util.Map;

public interface InformService extends IService<Inform> {

    /**
     * 查询消息及未读消息
     * @return
     */
    Map<String, Object> selectMessageInfo(InformRequestDTO dto, Paginator paginator);

    /**
     * 发送通知
     * @param sendType      发送类型1小时工2人力3用人单位4系统
     * @param acceptType    接收类型1小时工2人力3用人单位
     * @param content       通知内容
     * @param receiveId     接收方id
     * @param title         消息标题
     */
    void sendInformInfo(int sendType, int acceptType, String content, String receiveId, String title);

    /**
     * 根据接收方id查询通知数量
     * @param param
     * @return
     */
    int selectNoticeCountByReceiveId(Map<String, Object> param);

    /**
     * 根据条件查询数量
     * @param param
     * @return
     */
    int selectCountByParam(Map<String, Object> param);

    /**
     * 更新通知状态为已读
     * @param noticeId      通知id
     * @return
     */
    ResultDO updateInformStatus(String noticeId);

    /**
     * 删除通知
     * @param list
     * @return
     */
    ResultDO deleteInformInfo(List<String> list);
}
