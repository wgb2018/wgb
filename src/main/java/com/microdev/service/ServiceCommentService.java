package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.ServiceComment;
import com.microdev.param.CommentRequest;

public interface ServiceCommentService extends IService<ServiceComment> {

    /**
     * 提交评论
     * @param commentRequest
     * @return
     */
    public ResultDO serviceCommentSubmit(CommentRequest commentRequest);
}
