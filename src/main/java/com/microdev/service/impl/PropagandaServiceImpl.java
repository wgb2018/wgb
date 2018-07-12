package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.PropagandaMapper;
import com.microdev.model.Propaganda;
import com.microdev.service.PropagandaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PropagandaServiceImpl extends ServiceImpl<PropagandaMapper,Propaganda> implements PropagandaService {
}
