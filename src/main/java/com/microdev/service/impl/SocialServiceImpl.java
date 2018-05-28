package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.SocialMapper;
import com.microdev.model.Social;
import com.microdev.service.SocialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class SocialServiceImpl extends ServiceImpl<SocialMapper,Social> implements SocialService{
}
