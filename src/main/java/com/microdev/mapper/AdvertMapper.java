package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Advert;
import com.microdev.param.AdvertQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertMapper extends BaseMapper<Advert> {
    List<Advert> queryAdvert(AdvertQuery advertQuery);

}
