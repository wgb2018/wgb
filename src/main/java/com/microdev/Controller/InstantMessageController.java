package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.service.IMOperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstantMessageController {

    @Autowired
    private IMOperateService iMOperateService;

    /**
     * 处理账号无法登陆即时通讯
     * @param id 账号
     * @return
     */
    @GetMapping("/im/{id}/login_error")
    public ResultDO imLoginError(@PathVariable String id) {

        return iMOperateService.checkAccountInfo(id);
    }
}
