package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.model.Menu;
import com.microdev.param.MenuParam;
import com.microdev.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 新增菜单
     * @param menuParam
     * @return
     */
    @PostMapping("/agent/menu/create")
    public ResultDO createMenu(@RequestBody MenuParam menuParam) {

        return menuService.addMenu(menuParam);
    }

    /**
     * 修改菜单
     * @param menuParam
     * @return
     */
    @PostMapping("/agent/menu/update")
    public ResultDO updateMenu(@RequestBody MenuParam menuParam) {

        return menuService.modifyMenu(menuParam);
    }

    /**
     * 删除菜单
     * @return
     */
    @GetMapping("/agent/menu/{id}")
    public ResultDO deleteMenu(@PathVariable String id) {

        return menuService.deleteMenu(id);
    }

    /**
     * 查询菜单信息
     * @param pagingDO
     * @return
     */
    @PostMapping("/agent/menu/query")
    public ResultDO selectMenuInfo(PagingDO<Menu> pagingDO) {

        return menuService.selectByParam(pagingDO.getSelector(), pagingDO.getPaginator());
    }
}
