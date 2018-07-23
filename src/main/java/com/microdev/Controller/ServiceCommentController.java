package com.microdev.Controller;

import com.microdev.common.ResultDO;
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
}
