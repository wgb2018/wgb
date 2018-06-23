package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.VersionMapper;
import com.microdev.model.Version;
import com.microdev.param.VersionRequest;
import com.microdev.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Transactional
@Service
public class VersionServiceImpl extends ServiceImpl<VersionMapper,Version> implements VersionService {
    @Autowired
    VersionMapper versionMapper;
    @Override
    public ResultDO selectVersion(Paginator paginator, VersionRequest versionRequest) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize());
        //查询数据集合
        List<Version> list = versionMapper.queryVersions(versionRequest);
        PageInfo<Version> pageInfo = new PageInfo<>(list);
        HashMap<String, Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total", pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result", pageInfo.getList());
        result.put("page", paginator.getPage());
        return ResultDO.buildSuccess(result);
    }
}
