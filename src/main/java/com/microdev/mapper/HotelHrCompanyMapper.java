package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.HotelHrCompany;
import com.microdev.param.QueryCooperateRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HotelHrCompanyMapper extends BaseMapper<HotelHrCompany> {
    public void save(HotelHrCompany hotelHr);

    HotelHrCompany findOneHotelHr(@Param("hotelId")String hotelId, @Param("hrId")String hrId);

    public void update(HotelHrCompany hotelHr);

    int saveBatch(List<HotelHrCompany> list);

    List<Map<String, Object>> selectCooperateHr(QueryCooperateRequest map);
}
