package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Dict;
import com.microdev.model.UserArea;
import com.microdev.param.DictDTO;

import java.util.List;

public interface DictService extends IService<Dict> {
    DictDTO create(DictDTO dict);

    void update(DictDTO dict);

    void delete(String id);

    ResultDO findByName(String name);

    DictDTO getById(String id);

    ResultDO paging(Paginator paginator, DictDTO dictDTO);

    List<DictDTO> list();

    List<String> selectServiceTypeByUserId(String id);

    List <UserArea> findServiceArea(String id);
}
