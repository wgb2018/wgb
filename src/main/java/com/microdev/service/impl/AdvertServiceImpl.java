package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.AdvertMapper;
import com.microdev.model.Advert;
import com.microdev.model.Company;
import com.microdev.model.UserCompany;
import com.microdev.param.AdvertParam;
import com.microdev.param.AdvertQuery;
import com.microdev.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class AdvertServiceImpl extends ServiceImpl<AdvertMapper,Advert> implements AdvertService {

    @Autowired
    AdvertMapper advertMapper;
    @Override
    public ResultDO insertAdvert(AdvertParam param) {
        if(param == null){
            return ResultDO.buildError ("参数为空");
        }
        Advert advert = new Advert ();
        advert.setTittle (param.getTittle ());
        advert.setDescription (param.getDescription ());
        advert.setTheCover(param.getTheCover ());
        advert.setAdvertType (param.getAdvertType ());
        advert.setStatus (param.getStatus ());
        if(param.getStatus () == 1){
            advert.setReleaseTime (OffsetDateTime.now ());
        }
        if(param.getAdvertType () == 2){
            advert.setContent (param.getContent ());
        }else if(param.getAdvertType () == 3){
            advert.setExternalLinks (param.getExternalLinks ());
        }
        advertMapper.insert (advert);
        return ResultDO.buildSuccess ("添加成功");
    }

    @Override
    public ResultDO queryAdvert(Paginator paginator, AdvertQuery advertQuery) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Advert> list = advertMapper.queryAdvert(advertQuery);
        PageInfo<Advert> pageInfo = new PageInfo<>(list);
        System.out.println ("last:"+pageInfo.isHasNextPage ());
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }
}
