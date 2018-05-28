package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Company;
import com.microdev.param.CompanyQueryDTO;
import org.apache.ibatis.annotations.Param;import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyMapper extends BaseMapper<Company> {
    List<Company> queryCompanys(CompanyQueryDTO queryDTO);

    List<Company> queryHotelsByHrId(CompanyQueryDTO queryDTO);

    List<Company>  queryNotHotelsByHrId(String HrId);

    List<Company> queryCompanysByHotelId(CompanyQueryDTO queryDTO);

    List<Company> queryNotCompanysByHotelId(String HotelId);

    Company findCompanyById(String Id);

    void save(Company companyDTO);

    void update(Company companyDTO);

    List<Company> queryByworkerId(String userId);

    Company findFirstByLeaderMobile(String mobile);

	void insertAreaRelation(@Param("id") String id, @Param("code,") String code,@Param("level") Integer level);

    void insertCompanyArea(@Param("id") String id, @Param("areaId,") String area_id,@Param("idType") Integer idType);

}
