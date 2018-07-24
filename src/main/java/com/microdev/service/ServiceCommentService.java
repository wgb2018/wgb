package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.ServiceComment;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.CommentRequest;

public interface ServiceCommentService extends IService<ServiceComment> {

    /**
     * 提交评论
     * @param commentRequest
     * @return
     */
    public ResultDO serviceCommentSubmit(CommentRequest commentRequest);

    /**
     * 查看角色信用记录
     * @param paginator
     * @param param
     * @return
     */
    public ResultDO selectCommentInfo(Paginator paginator, ApplyParamDTO param);

    /**
     * 查看人力/用人单位信用记录
     * @param paginator
     * @param param
     * @return
     */
    public ResultDO selectPcCommentInfo(Paginator paginator, ApplyParamDTO param);
}
