package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.*;
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
    public ResultDO selectMessageDetails(@RequestBody MessageDetailsDTO dto) {

        return ResultDO.buildSuccess(messageService.selectMessageDetails(dto.getMessageId(), dto.getMessagetype(), dto.getType()));
    }

    /**
     * 查询消息详情的任务信息
     * @return
     */
    @PostMapping("/message/details/taskInfo")
    public ResultDO selectDetailTaskInfo(@RequestBody MessageDetailsDTO dto) {
        return ResultDO.buildSuccess(messageService.selectAwaitTaskDetails(dto.getMessageId(), dto.getMessagetype(), dto.getType()));
    }

    /**
     * pc端查询申请调配信息.
     * @param paging
     * @return
     */
    @PostMapping("/message/deploy/apply")
    public ResultDO selectDeployApply(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return ResultDO.buildSuccess(messageService.selectDeployApply(paging));
    }

    /**
     * 手机端首页展示未读数量
     * @param id            角色id
     * @param applyType     角色类型小时工传worker,酒店传hotel，人力传hr
     * @return
     */
    @GetMapping("/message/{id}/amount/{applyType}")
    public ResultDO selectMessageAmount(@PathVariable String id,@PathVariable String applyType) {

        return ResultDO.buildSuccess(messageService.selectUnReadCount(id, applyType));
    }

    /**
     * PC查询申请补签
     * @param paging
     * @return
     */
    @PostMapping("/message/pc/supplement")
    public ResultDO selectPcSupplementInfo(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return messageService.selectPcSupplement(paging);
    }

    /**
     *pc查询请假申请
     * @param paging
     * @return
     */
    @PostMapping("/message/pc/leave")
    public ResultDO selectPcLeaveInfo(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return messageService.selectPcLeaveApply(paging.getSelector(), paging.getPaginator());
    }

    /**
     * pc查询加班申请
     * @param paging
     * @return
     */
    @PostMapping("/message/pc/extra/apply")
    public ResultDO selectPcExtraApply(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return messageService.selectPcExtraApply(paging.getSelector(), paging.getPaginator());
    }

    /**
     * pc查询解绑申请
     * @param paging
     * @return
     */
    @PostMapping("/message/pc/unbind/apply")
    public ResultDO selectPcUnbind(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return messageService.selectPcUnBindApply(paging.getSelector(), paging.getPaginator());
    }

    /**
     * PC端申请绑定
     * @param paging
     * @return
     */
    @PostMapping("/message/pc/bind/apply")
    public ResultDO selectPcBind(@RequestBody PagingDO<ApplyParamDTO> paging) {

        return messageService.selectPcBindApply(paging.getSelector(), paging.getPaginator());
    }

}
