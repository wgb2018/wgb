package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.utils.StringKit;
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
    public void sendInform(String acceptype, String receiveTd, InformType informType,Map<String, String> templateParams) {
        InformTemplate param = new InformTemplate ();
        param.setCode (informType);
        InformTemplate informTemplate = informTemplateMapper.selectOne (param);
        if(informTemplate == null){
            //throw  new Exception ("没有找到通知模板");
        }
        String content = StringKit.templateReplace(informTemplate.getContent(), templateParams);
        Inform inform = new Inform();
        //nform.

    }
}
