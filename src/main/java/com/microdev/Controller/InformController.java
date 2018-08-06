package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.param.InformParam;
import com.microdev.param.InformRequestDTO;
import com.microdev.service.InformService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InformController {

    @Autowired
    private InformService informService;

    /**
     * 查询角色通知
     * @param paging
     * @return
     */
    @PostMapping("/inform/show/details")
    public ResultDO getInformDetails(@RequestBody PagingDO<InformRequestDTO> paging) {

        return ResultDO.buildSuccess(informService.selectMessageInfo(paging.getSelector(), paging.getPaginator()));
    }

    /**
     * 更新通知状态为已读
     * @param noticeId  通知id
     * @return
     */
    @GetMapping("/inform/update/{noticeId}")
    public ResultDO updateinformNotice(@PathVariable String noticeId) {

        return informService.updateInformStatus(noticeId);
    }

    /**
     * 删除通知
     * @param inf
     * @return
     */
    @PostMapping("/inform/delete")
    public ResultDO deleteInformInfo(@RequestBody InformParam inf) {
        return informService.deleteInformInfo(inf.getList ());
    }
}
