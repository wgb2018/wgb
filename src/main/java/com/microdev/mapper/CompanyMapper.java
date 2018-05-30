package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Company;
import com.microdev.param.CompanyQueryDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CompanyMapper extends BaseMapper<Company> {
    List<Company> queryCompanys(CompanyQueryDTO queryDTO);

    List<Map<String, Object>> queryHotelsByHrId(CompanyQueryDTO queryDTO);

    List<Company>  queryNotHotelsByHrId(String HrId);

    List<Map<String, Object>> queryCompanysByHotelId(CompanyQueryDTO queryDTO);

    List<Company> queryNotCompanysByHotelId(String HotelId);

    Company findCompanyById(String Id);

    void save(Company companyDTO);

    void update(Company companyDTO);

    List<Company> queryByworkerId(@Param("userId") String userId);

    Company findFirstByLeaderMobile(@Param("mobile") String mobile);

    void insertAreaRelation(@Param("id") String id, @Param("code,") String code,@Param("level") Integer level,@Param("name") String name);

    void insertCompanyArea(@Param("id") String id, @Param("areaId,") String area_id,@Param("idType") Integer idType);

    void deleteAreaRelation(String id);

    void deleteCompanyArea(String id);

}
