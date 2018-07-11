package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.model.Directory;
import com.microdev.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DirectoryController {
    @Autowired
    private DirectoryService directoryService;
    /**
     * 编辑用户指南
     */
    @PostMapping("/directory/edit")
    public ResultDO edit(@RequestBody Directory directory) {
        return null;
    }
    /**
     * 添加用户指南
     */
    @PostMapping("/directory/add")
    public ResultDO add(@RequestBody Directory directory) {
        return null;
    }
    /**
     * 删除用户指南
     */
    @PostMapping("/directory/delete")
    public ResultDO delete(@RequestBody Directory directory) {
        return null;
    }
    /**
     * 查看用户指南
     */
    @PostMapping("/directory/query")
    public ResultDO query(@RequestBody Directory directory) {
        return directoryService.selectAll(directory);
    }

}
