package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.ServiceCommentMapper;
import com.microdev.model.ServiceComment;
import com.microdev.service.ServiceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceCommentServiceImpl extends ServiceImpl<ServiceCommentMapper, ServiceComment> implements ServiceCommentService {

    @Autowired
    private ServiceCommentMapper serviceCommentMapper;
}
