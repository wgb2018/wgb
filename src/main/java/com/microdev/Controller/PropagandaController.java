package com.microdev.Controller;


import com.microdev.common.ResultDO;
import com.microdev.param.CreateMsgTemplateRequest;
import com.microdev.service.PropagandaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PropagandaController {
    @Autowired
    PropagandaService propagandaService;

}
