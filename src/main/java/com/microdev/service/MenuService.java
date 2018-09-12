package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Menu;
import com.microdev.param.MenuParam;

public interface MenuService extends IService<Menu> {

    /**
     * 新增菜单
     * @param menuParam
     * @return
     */
    public ResultDO addMenu(MenuParam menuParam);

    /**
     * 修改菜单
     * @param menuParam
     * @return
     */
    public ResultDO modifyMenu(MenuParam menuParam);

    /**
     * 删除菜单
     * @return
     */
    public ResultDO deleteMenu(String id);

    /**
     * 查询菜单信息。
     * @param menu
     * @param paginator
     * @return
     */
    public ResultDO selectByParam(Menu menu, Paginator paginator);
}
