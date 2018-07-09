package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.mapper.DictMapper;
import com.microdev.param.DictDTO;
import com.microdev.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 数据字典后台管理
 */
@RestController
@RequestMapping("/admin/dicts")
public class DictAdminController {
    @Autowired
    private DictService dictService;
    @Autowired
    DictMapper dictMapper;
    /**
     * 创建字典
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ResultDO create(@RequestBody DictDTO dictDTO) {
        return ResultDO.buildSuccess(dictService.create(dictDTO));
    }

    /**
     * 修改字典
     */
    @PostMapping("/update")
    public ResultDO update(@RequestBody DictDTO dictDTO) {
        dictService.update(dictDTO);
        return ResultDO.buildSuccess("修改成功");
    }

    /**
     * 删除字典
     */
    @GetMapping("/delete/{id}")
    public ResultDO delete(@PathVariable String id) {
        dictService.delete(id);
        return ResultDO.buildSuccess("删除成功");
    }

    /**
     * 获取字典详情
     */
    @GetMapping("/{id}")
    public ResultDO getById(@PathVariable String id) {
        return ResultDO.buildSuccess(dictService.getById(id));
    }

    /**
     * 分页查询字典
     */
    @PostMapping("/search")
    public ResultDO paging(@RequestBody PagingDO<DictDTO> paging) {
        return dictService.paging(paging.getPaginator(), paging.getSelector());
    }

    /**
     * 获取所有字典
     */
    @PostMapping("/list")
    public ResultDO list() {
        return ResultDO.buildSuccess(dictService.list());
    }

    /**
     * 根据名称获取字典
     */
    @GetMapping("/findbyname/{name}")
    public ResultDO getByName(@PathVariable String name) {
        return  dictService.findByName(name);
    }
    /**
     * 查询地区一级列表
     */
    @GetMapping("/findprovince")
    public ResultDO getProvince() {
        return  ResultDO.buildSuccess(dictMapper.findProvince());
    }
    /**
     * 查询地区二级列表
     */
    @GetMapping("/findcity/{provinceId}")
    public ResultDO getCity(@PathVariable String provinceId) {
        return  ResultDO.buildSuccess(dictMapper.findCity(provinceId));
    }
    /**
     * 查询地区三级列表
     */
    @GetMapping("/findarea/{cityId}")
    public ResultDO getArea(@PathVariable String cityId) {
        return  ResultDO.buildSuccess(dictMapper.findArea(cityId));
    }
	/**
     * 获取用户的服务地区
     */
    @GetMapping("/findServiceArea/{userId}")
    public ResultDO getUserArea(@PathVariable String userId) {
        return  ResultDO.buildSuccess(dictService.findServiceArea(userId));
    }

}
