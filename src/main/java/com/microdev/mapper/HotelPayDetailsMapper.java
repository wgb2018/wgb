package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.HotelPayHrDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelPayDetailsMapper extends BaseMapper<HotelPayHrDetails> {
    void save(HotelPayHrDetails hotelPayHrDetails);
}
