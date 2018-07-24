package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.CommentRequest;
import com.microdev.service.ServiceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceCommentController {

    @Autowired
    private ServiceCommentService serviceCommentService;

    /**
     * 提交评论
     * @param commentRequest
     * @return
     */
    @PostMapping("/comment/submit")
    public ResultDO ServiceComment(@RequestBody CommentRequest commentRequest) {

        return serviceCommentService.serviceCommentSubmit(commentRequest);
    }

    /**
     * 查看角色信用记录
     * @param pagingDO
     * @return
     */
    @PostMapping("/comment/search")
    public ResultDO searchConsumerComment(@RequestBody PagingDO<ApplyParamDTO> pagingDO) {

        return serviceCommentService.selectCommentInfo(pagingDO.getPaginator(), pagingDO.getSelector());
    }

    /**
     * pc端查看人力/用人单位信用记录
     * @param pagingDO
     * @return
     */
    @PostMapping("/comment/search/pc")
    public ResultDO searchCommentPc(@RequestBody PagingDO<ApplyParamDTO> pagingDO) {

        return serviceCommentService.selectPcCommentInfo(pagingDO.getPaginator(), pagingDO.getSelector());
    }
}
