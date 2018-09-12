package com.microdev.service;


import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Power;
import com.microdev.param.PowerParam;

public interface PowerService extends IService<Power> {

    /**
     * 新增权限
     * @param powerParam
     * @return
     */
    public ResultDO addPower(PowerParam powerParam);

    /**
     * 修改权限
     * @param powerParam
     * @return
     */
    public ResultDO modifyPower(PowerParam powerParam);

    /**
     * 删除权限
     * @param id
     * @return
     */
    public ResultDO deletePower(String id);

    /**
     * 查询权限信息
     * @param power
     * @param paginator
     * @return
     */
    public ResultDO selectByParam(Power power, Paginator paginator);
}
