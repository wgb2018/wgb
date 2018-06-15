package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.model.Advert;
import com.microdev.param.AdvertParam;
import com.microdev.param.AdvertQuery;

public interface AdvertService extends IService<Advert> {

    ResultDO insertAdvert(AdvertParam param) throws Exception;

    ResultDO queryAdvert(Paginator paginator, AdvertQuery advertQuery);
}
