package com.microdev.Controller;

import com.microdev.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InformController {

    @Autowired
    private InformService informService;
}
