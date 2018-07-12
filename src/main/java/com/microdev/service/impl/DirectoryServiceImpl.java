package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.mapper.DirectoryMapper;
import com.microdev.model.Directory;
import com.microdev.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class DirectoryServiceImpl extends ServiceImpl<DirectoryMapper,Directory> implements DirectoryService {
    @Autowired
    DirectoryMapper directoryMapper;
    @Override
    public ResultDO selectAll(Directory directory) {
        return ResultDO.buildSuccess (directoryMapper.selectByDirectory(directory));
    }
}
