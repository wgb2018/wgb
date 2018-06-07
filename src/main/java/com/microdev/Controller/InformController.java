package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.InformRequestDTO;
import com.microdev.service.InformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InformController {

    @Autowired
    private InformService informService;

    /**
     * 查询消息
     * @param paging
     * @return
     */
    @PostMapping("/inform/show/details")
    public ResultDO getInformDetails(@RequestBody PagingDO<InformRequestDTO> paging) {

        return ResultDO.buildSuccess(informService.selectMessageInfo(paging.getSelector(), paging.getPaginator()));
    }
}
