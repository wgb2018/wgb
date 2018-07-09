package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.HotelHrCompany;
import com.microdev.param.HotelHrIdBindDTO;
import com.microdev.param.QueryCooperateRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    int selectIsBind(HotelHrIdBindDTO dto);

    int selectIsBIndByCompanyId(HotelHrIdBindDTO dto);

    @Select("select count(1) from hotel_hr_company where hr_id = #{hrId} and hotel_id = #{hotelId} and status in (0,3)")
    int selectHrHotelId(String hrId, String hotelId);

    int selectBindCountByHotelId(HotelHrIdBindDTO dto);

    int selectRelieveCountByHotelId(HotelHrIdBindDTO dto);

    int selectRelieveCountByHrId(HotelHrIdBindDTO dto);

    int selectBindCountByHrId(HotelHrIdBindDTO dto);

    HotelHrCompany selectByHrHotelId(@Param("hrId") String hrId,@Param("hotelId") String hotelId);
}
