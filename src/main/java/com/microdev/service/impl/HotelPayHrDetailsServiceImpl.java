package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.mapper.HotelPayDetailsMapper;
import com.microdev.model.HotelPayHrDetails;
import com.microdev.service.HotelPayHrDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class HotelPayHrDetailsServiceImpl extends ServiceImpl<HotelPayDetailsMapper, HotelPayHrDetails>
            implements HotelPayHrDetailsService {

    @Autowired
    private HotelPayDetailsMapper hotelPayDetailsMapper;
    /**
     * 新增用人单位人力信息。
     * @param details
     */
    @Override
    public void saveBean(HotelPayHrDetails details) {
        hotelPayDetailsMapper.save(details);
    }
}
