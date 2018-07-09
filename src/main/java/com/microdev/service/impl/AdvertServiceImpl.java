package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.FileUtil;
import com.microdev.common.utils.ForFile;
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

import java.io.File;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class AdvertServiceImpl extends ServiceImpl<AdvertMapper,Advert> implements AdvertService {

    @Autowired
    AdvertMapper advertMapper;
    @Autowired
    ObjectStoreService objectStoreService;
    @Override
    public ResultDO insertAdvert(AdvertParam param) throws Exception{
        if(param == null){
            return ResultDO.buildError ("参数为空");
        }
        Advert advert = new Advert ();
        advert.setTitle (param.getTitle ());
        advert.setDescription (param.getDescription ());
        advert.setTheCover(param.getTheCover ());
        advert.setAdvertType (param.getAdvertType ());
        advert.setStatus (param.getStatus ());
        advert.setLocation (param.getLocation ());
        if(param.getStatus () == 1){
            advert.setReleaseTime (OffsetDateTime.now ());
        }
        if(param.getAdvertType () == 2){
            /*File file = ForFile.createFile (param.getContent (),".html");
            String filePath = "file".toLowerCase() + "/" + FileUtil.fileNameReplaceSHA1(file);
            //文件上传成功后返回的下载路径，比如: http://oss.xxx.com/avatar/3593964c85fd76f12971c82a411ef2a481c9c711.jpg
            String fileURI = objectStoreService.uploadFile (filePath, file);
            //返回地址给前端
            advert.setContent (fileURI);*/
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

        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }
}
