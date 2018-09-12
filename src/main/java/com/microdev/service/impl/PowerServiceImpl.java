package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.PowerMapper;
import com.microdev.mapper.PowerMenuMapper;
import com.microdev.model.Power;
import com.microdev.model.PowerMenu;
import com.microdev.param.PowerParam;
import com.microdev.service.PowerService;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@Transactional
@Service
public class PowerServiceImpl extends ServiceImpl<PowerMapper, Power> implements PowerService {

    @Autowired
    private PowerMapper powerMapper;
    @Autowired
    private PowerMenuMapper powerMenuMapper;

    /**
     * 新增权限
     * @param powerParam
     * @return
     */
    @Override
    public ResultDO addPower(PowerParam powerParam) {

        if (powerParam == null || powerParam.getLevel() == null || StringUtils.isEmpty(powerParam.getName()) || powerParam.getSet() == null || powerParam.getSet().size() == 0) {
            throw new ParamsException("参数不能为空");
        }
        Power power = new Power();
        power.setCode(powerParam.getCode());
        power.setLevel(powerParam.getLevel());
        power.setName(powerParam.getName());
        power.setIdentifer(powerParam.getIdentifer());
        powerMapper.insert(power);
        String powerId = power.getPid();
        Set<String> set = powerParam.getSet();
        PowerMenu powerMenu = null;
        for (String menuId : set) {
            powerMenu = new PowerMenu();
            powerMenu.setMenuId(menuId);
            powerMenu.setPowerId(powerId);
            powerMenuMapper.insert(powerMenu);
        }
        return ResultDO.buildSuccess("新增成功");
    }

    /**
     * 修改权限
     * @param powerParam
     * @return
     */
    @Override
    public ResultDO modifyPower(PowerParam powerParam) {

        if (powerParam == null || StringUtils.isEmpty(powerParam.getId())) {
            throw new ParamsException("参数不能为空");
        }
        String powerId = powerParam.getId();
        Power power = powerMapper.selectById(powerId);
        if (power == null) {
            return ResultDO.buildError("查询不到权限信息");
        } else {
            power.setName(powerParam.getName());
            power.setLevel(powerParam.getLevel());
            power.setCode(powerParam.getCode());
            powerMapper.updateById(power);
            powerMenuMapper.deleteByPowerId(powerId);
            Set<String> set = powerParam.getSet();
            if (set != null && set.size() > 0) {
                List<PowerMenu> list = new ArrayList<>();
                for (String menuId : set) {
                    PowerMenu powerMenu = new PowerMenu();
                    powerMenu.setPowerId(powerId);
                    powerMenu.setMenuId(menuId);
                    list.add(powerMenu);
                }
                powerMenuMapper.insertBatch(list);
            }
        }
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 删除权限
     * @param id
     * @return
     */
    @Override
    public ResultDO deletePower(String id) {

        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数不能为空");
        }
        Power power = powerMapper.selectById(id);
        if (power == null) {
            return ResultDO.buildError("查询不到权限信息");
        }
        power.setDeleted(true);
        powerMapper.updateById(power);
        powerMenuMapper.deleteByPowerId(id);
        return ResultDO.buildError("删除成功");
    }

    /**
     *查询权限信息
     * @param power
     * @param paginator
     * @return
     */
    @Override
    public ResultDO selectByParam(Power power, Paginator paginator) {

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<Power> list = powerMapper.selectByParam(power);
        Map<String, Object> result = new HashMap<>();
        PageInfo<Power> pageInfo = new PageInfo<>(list);
        result.put("total", pageInfo.getTotal());
        result.put("page",paginator.getPage());
        result.put("result", list);
        return ResultDO.buildSuccess(result);
    }
}
