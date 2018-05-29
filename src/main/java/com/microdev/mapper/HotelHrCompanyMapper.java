package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.HotelHrCompany;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelHrCompanyMapper extends BaseMapper<HotelHrCompany> {
    public void save(HotelHrCompany hotelHr);

    HotelHrCompany findOneHotelHr(@Param("hotelId")String hotelId, @Param("hrId")String hrId);

    public void update(HotelHrCompany hotelHr);

    int saveBatch(List<HotelHrCompany> list);
}
