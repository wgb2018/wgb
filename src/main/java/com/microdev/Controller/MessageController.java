package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.param.MessageQuery;
import com.microdev.param.PageRequest;
import com.microdev.param.QueryCooperateRequest;
import com.microdev.service.MessageService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @GetMapping("/messages/{id}/amount/{applyType}")
    public ResultDO getMessageAmount(@PathVariable String id, @PathVariable String applyType) {

        return ResultDO.buildSuccess(messageService.selectUnReadCount(id, applyType));
    }

    /**
     * 用户查询未读消息
     * @param id
     * @param applyType
     * @return
     */
    @GetMapping("/messages/{id}/info/{applyType}")
    public ResultDO getUnReadMessageInfo(@PathVariable String id,@PathVariable String applyType) {

        return ResultDO.buildSuccess(messageService.selectUnReadMessage(id, applyType));
    }

    /**
     * 查询消息
     * @param request
     * @return
     */
    @PostMapping("/messages/select/messsageInfo")
    public ResultDO getMessageInfo(PageRequest request) {

        return ResultDO.buildSuccess(messageService.selectMessage(request.getId(), request.getRole(), request.getType(), request.getPage(), request.getPage()));
    }

    /**
     * 更新消息的已读标识
     * @param id        消息id
     * @return
     */
    @GetMapping("/messages/{id}/checkSign")
    public ResultDO updateMessageCheckSign(@PathVariable String id) {
        return ResultDO.buildSuccess(messageService.updateMsgStatus(id));
    }

    /**
     *分页查询待处理事务
     * @return
     */
    @PostMapping("/message/await/handle")
    public ResultDO selectWaitHandleWork(@RequestBody PagingDO<QueryCooperateRequest> paging) {

        return messageService.showWaitHandleWork(paging.getSelector(), paging.getPaginator());
    }

    /**
     * 查询消息详情的申请信息。
     */
    @PostMapping("/message/details/applyInfo")
    public ResultDO selectMessageDetails(Map<String, String> map) {

        return ResultDO.buildSuccess(messageService.selectMessageDetails(map.get("messageId"), map.get("messagetype"), map.get("type")));
    }

    /**
     * 查询消息详情的任务信息
     * @return
     */
    @PostMapping("/message/details/taskInfo")
    public ResultDO selectDetailTaskInfo(Map<String, String> map) {

        return ResultDO.buildSuccess(messageService.selectAwaitTaskDetails(map.get("messageId"), map.get("messagetype"), map.get("type")));
    }
}
