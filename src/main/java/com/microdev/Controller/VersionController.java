package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.mapper.VersionMapper;
import com.microdev.model.Version;
import com.microdev.param.TaskHrQueryDTO;
import com.microdev.param.VersionRequest;
import com.microdev.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 版本管理Api
 */
@RestController
public class VersionController {
    @Autowired
    VersionService versionService;
    @Autowired
    VersionMapper versionMapper;
    /**
     * 添加版本记录
     */
    @PostMapping("/version/add")
    public ResultDO insertVersion(@RequestBody VersionRequest request){
        Version version = new Version();
        version.setAddress (request.getAddress ());
        version.setContent (request.getContent ());
        version.setIsUpdate (request.getIsUpdate ());
        version.setType (request.getType ());
        version.setVersionCode (request.getVersionCode ());
        versionService.insert (version);
        return ResultDO.buildSuccess ("添加成功");
    }
    /**
     * 更新版本记录
     */
    @PostMapping("/version/edit")
    public ResultDO updateVersion(@RequestBody VersionRequest request) throws Exception{
        Version version = versionService.selectById (request.getId ());
        if(version == null){
            throw new Exception ("更新版本错误");
        }
        version.setAddress (request.getAddress ());
        version.setContent (request.getContent ());
        version.setIsUpdate (request.getIsUpdate ());
        version.setType (request.getType ());
        version.setVersionCode (request.getVersionCode ());
        versionService.updateById (version);
        return ResultDO.buildSuccess ("更新成功");
    }
    /**
     * 查询版本记录
     */
    @PostMapping("/search/version")
    public ResultDO selectVersion(@RequestBody PagingDO<VersionRequest> paging) throws Exception{
        return versionService.selectVersion(paging.getPaginator(),paging.getSelector());
    }

    @GetMapping("/version/{type}")
    public ResultDO version(@PathVariable String type){
        return ResultDO.buildSuccess (versionMapper.version(type));
    }
}
