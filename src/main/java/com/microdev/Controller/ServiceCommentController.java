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

    @PostMapping("/comment/submit")
    public ResultDO ServiceComment(@RequestBody CommentRequest commentRequest) {

        return ResultDO.buildSuccess("");
    }
}
