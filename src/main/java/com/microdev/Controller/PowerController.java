package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.model.Power;
import com.microdev.param.PowerParam;
import com.microdev.service.PowerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PowerController {

    @Autowired
    private PowerService powerService;

    /**
     * 新增权限
     * @param powerParam
     * @return
     */
    @PostMapping("/agent/power/create")
    public ResultDO createPower(@RequestBody PowerParam powerParam) {

        return powerService.addPower(powerParam);
    }

    /**
     * 修改权限
     * @param powerParam
     * @return
     */
    @PostMapping("/agent/power/update")
    public ResultDO updatePower(@RequestBody PowerParam powerParam) {

        return powerService.modifyPower(powerParam);
    }

    /**
     * 删除权限
     * @param id
     * @return
     */
    @GetMapping("/agent/power/{id}")
    public ResultDO deletePower(@PathVariable String id) {

        return powerService.deletePower(id);
    }

    /**
     * 查询权限信息
     * @param pagingDO
     * @return
     */
    @PostMapping("/agent/power/query")
    public ResultDO selectPowerInfo(PagingDO<Power> pagingDO) {

        return powerService.selectByParam(pagingDO.getSelector(), pagingDO.getPaginator());
    }
}
