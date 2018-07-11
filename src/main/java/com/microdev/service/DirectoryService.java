package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.Directory;

import java.util.List;


public interface DirectoryService extends IService<Directory> {
    ResultDO selectAll(Directory directory);
}
