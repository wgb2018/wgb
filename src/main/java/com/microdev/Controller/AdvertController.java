package com.microdev.Controller;

import com.microdev.common.PagingDO;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.AdvertMapper;
import com.microdev.model.Advert;
import com.microdev.param.AdvertParam;
import com.microdev.param.AdvertQuery;
import com.microdev.param.QueryCooperateRequest;
import com.microdev.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdvertController {
    /**
     * 广告添加
     */
    @Autowired
    AdvertService advertService;
    @Autowired
    AdvertMapper advertMapper;
    @PostMapping("/advert/add")
    public ResultDO addAdvert(@RequestBody AdvertParam param) {
        return  ResultDO.buildSuccess(advertService.insertAdvert (param));
    }
    /**
     * 删除广告
     */
    @GetMapping("/advert/delete/{id}")
    public ResultDO deleteAdvert(@PathVariable String id) {
        advertService.deleteById (id);
        return ResultDO.buildSuccess ("删除成功");
    }
    /**
     * 查询广告列表
     */
    @PostMapping("/search/advert")
    public ResultDO queryAdvert(@RequestBody PagingDO<AdvertQuery> page) {
        return ResultDO.buildSuccess (advertService.queryAdvert (page.getPaginator (),page.getSelector ()));
    }
    /**
     * 广告更新
     */
    @PostMapping("/advert/update")
    public ResultDO updateAdvert(@RequestBody AdvertParam param) throws Exception{
        Advert advert = advertMapper.selectById (param.getPid ());
        if(advert == null){
            throw new Exception ("无法查到需要更新的广告信息");
        }
        advert.setExternalLinks (param.getExternalLinks ());
        advert.setContent (param.getContent ());
        advert.setDescription (param.getDescription ());
        advert.setTittle (param.getTittle ());
        advert.setStatus (param.getStatus ());
        advert.setTheCover (param.getTheCover ());
        advert.setAdvertType (param.getAdvertType ());
        advertService.updateById (advert);
        return ResultDO.buildSuccess ("更新成功");
    }
    /**
     * 查询单条广告
     */
    @GetMapping("/advert/detail/{id}")
    public ResultDO detailAdvert(@PathVariable String id) throws Exception{
        return ResultDO.buildSuccess (advertMapper.selectById (id));
    }



}
