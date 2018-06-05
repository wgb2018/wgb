package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.InformMapper;
import com.microdev.mapper.InformTemplateMapper;
import com.microdev.model.Inform;
import com.microdev.model.InformTemplate;
import com.microdev.param.InformType;
import com.microdev.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class InformServiceImpl extends ServiceImpl<InformMapper,Inform>  implements InformService {
    @Autowired
    private InformTemplateMapper informTemplateMapper;
    @Override
    public void sendInform(String acceptype, String receiveTd, InformType informType) {
        Map<String,Object> map = new HashMap<String,Object> ();
        map.put ("code",informType);
        //InformTemplate informTemplate = informTemplateMapper.selectByMap (map);
    }
}
