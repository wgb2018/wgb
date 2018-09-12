package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.MenuMapper;
import com.microdev.mapper.MenuModelMapper;
import com.microdev.model.Menu;
import com.microdev.model.MenuModel;
import com.microdev.param.MenuParam;
import com.microdev.service.MenuService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Transactional
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private MenuModelMapper menuModelMapper;

    /**
     * 新增菜单
     * @param menuParam
     * @return
     */
    @Override
    public ResultDO addMenu(MenuParam menuParam) {

        if (menuParam == null || StringUtils.isEmpty(menuParam.getName()) || menuParam.getLevel() == null || menuParam.getStatus() == null || menuParam.getSet().size() == 0) {
            throw new ParamsException("参数错误");
        }
        Menu menu = new Menu();
        menu.setLevel(menuParam.getLevel());
        menu.setName(menuParam.getName());
        menu.setStatus(menuParam.getStatus());
        menu.setAgentId(menuParam.getId());
        menuMapper.insert(menu);
        //菜单下包含的模块
        MenuModel menuModel = null;
        String menuId = menu.getPid();
        for (Integer modelId : menuParam.getSet()) {
            menuModel = new MenuModel();
            menuModel.setMenuId(menuId);
            menuModel.setModelId(modelId);
            menuModelMapper.insert(menuModel);
        }
        return ResultDO.buildSuccess("新增成功");
    }

    /**
     * 修改菜单
     * @param menuParam
     * @return
     */
    @Override
    public ResultDO modifyMenu(MenuParam menuParam) {

        if (menuParam == null || StringUtils.isEmpty(menuParam.getName()) || menuParam.getLevel() == null || StringUtils.isEmpty(menuParam.getId())) {
            throw new ParamsException("参数错误");
        }
        Menu menu = menuMapper.selectById(menuParam.getId());
        if (menu == null) {
            return ResultDO.buildError("查询不到菜单信息");
        } else {
            menu.setName(menuParam.getName());
            menu.setLevel(menuParam.getLevel());
            menu.setStatus(menuParam.getStatus());
            menuMapper.updateById(menu);
            String menuId = menuParam.getId();
            menuModelMapper.deleteByMenuId(menuId);

            if (menuParam.getSet() != null && menuParam.getSet().size() > 0) {
                List<MenuModel> list = new ArrayList<>();
                for (Integer modelId : menuParam.getSet()) {
                    MenuModel menuModel = new MenuModel();
                    menuModel.setMenuId(menuId);
                    menuModel.setModelId(modelId);
                    list.add(menuModel);
                }
                menuModelMapper.insertBatch(list);
            }
        }
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 删除菜单
     * @param id
     * @return
     */
    @Override
    public ResultDO deleteMenu(String id) {

        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数不能为空");
        }
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            return ResultDO.buildError("查询不到这个菜单");
        }
        menu.setDeleted(true);
        menuMapper.updateById(menu);
        menuModelMapper.deleteByMenuId(id);
        return ResultDO.buildSuccess("删除成功");
    }

    /**
     * 查询菜单信息
     * @param menu
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectByParam(Menu menu, Paginator paginator) {

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<Menu> list = menuMapper.selectByparam(menu);
        Map<String, Object> result = new HashMap<>();
        PageInfo<Menu> pageInfo = new PageInfo<>(list);
        result.put("total", pageInfo.getTotal());
        result.put("page",paginator.getPage());
        result.put("result", list);
        return ResultDO.buildSuccess(result);
    }
}
