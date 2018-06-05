package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.model.Inform;
import com.microdev.param.InformType;

public interface InformService extends IService<Inform> {
    void sendInform(String acceptype, String receiveTd, InformType informType);
}
