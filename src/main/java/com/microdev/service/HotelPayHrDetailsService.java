package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.model.HotelPayHrDetails;


public interface HotelPayHrDetailsService extends IService<HotelPayHrDetails> {

    /**
     * 新增酒店人力信息。
     * @param details
     */
    void saveBean(HotelPayHrDetails details);
}
