package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.MessageQuery;
import com.microdev.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author yinbaoxin
 */
@RestController
public class MessageController {

    @Autowired
    MessageService messageService;
    /**
     * 创建消息模板
     */
    @PostMapping("/message-templates")
    public ResultDO createMsgTemplate(@RequestBody CreateMsgTemplateRequest request) {
        return messageService.createMsgTemplate(request);
    }
    /**
     * 修改消息模板
     */
    @PutMapping("/message-templates")
    public ResultDO updateMsgTemplate(@RequestBody CreateMsgTemplateRequest request) {
        return messageService.updateMsgTemplate(request);
    }
    /**
     * 根据code查询消息模板
     */
    @GetMapping("/message-templates/{code}")
    public ResultDO getMsgTemplateByCode(@PathVariable String code) {
        return messageService.getMsgTemplateByCode(code);
    }
    /**
     * 查询所有的消息模板
     */
    @GetMapping("/message-templates")
    public ResultDO getAllMsgTemplate() {
        return messageService.getAllMsgTemplate();
    }
    /**
     * 分页查询消息
     */
    @PostMapping("/messages/search")
    public ResultDO getPageData(@RequestBody PagingDO<MessageQuery> paging) {
        return messageService.getPageMessages(paging.getPaginator(),paging.getSelector());
    }
    /**
     * 修改消息为已读状态
     */
    @PutMapping("/messages/{id}/status")
    public ResultDO editMessageStatus(@PathVariable String id) {
        return messageService.updateMsgStatus(id);
    }

    /**
     * 未读消息数量
     * @param id
     * @param applyType
     * @return
     */
    @GetMapping("/messages/unread/amount")
    public ResultDO getMessageAmount(String id, String applyType) {

        return ResultDO.buildSuccess(messageService.selectUnReadCount(id, applyType));
    }

    /**
     * 用户查询未读消息
     * @param id
     * @param applyType
     * @return
     */
    @GetMapping("/messages/unread/messsageInfo")
    public ResultDO getUnReadMessageInfo(String id, String applyType) {

        return ResultDO.buildSuccess(messageService.selectUnReadMessage(id, applyType));
    }
}
