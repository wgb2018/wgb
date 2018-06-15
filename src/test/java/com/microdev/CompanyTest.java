package com.microdev;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.Criteria;
import com.microdev.mapper.AreaRelationMapper;
import com.microdev.param.CompanyQueryDTO;
import com.microdev.param.HotelHandleWorkerRecord;
import com.microdev.service.MessageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit4.SpringRunner;

import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.CompanyMapper;
import com.microdev.model.Company;
import com.microdev.param.HotelDeployInfoRequest;
import com.microdev.service.CompanyService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CompanyTest {

	@Autowired
	private CompanyService companyService;
	@Autowired
    private CompanyMapper companyMapper;
	@Autowired
	private MessageService messageService;

	@Test
	public void pageTest() {
		Paginator p = new Paginator();
		p.setPage(1);
		p.setPageSize(5);
        CompanyQueryDTO c = new CompanyQueryDTO();
		c.setDeleted(false);
		c.setCompanyType(2);
		c.setObserverId("185b78fe-c3c4-4dc7-a48e-36b9d163c3f2");
		ResultDO r = companyService.pagingCompanys(p, c);
		System.out.println(r.toString());
	}

	
	@Test
	public void threeTest() {
        Map<String, Object> map = new HashMap<>();
		map.put("status", 0);

		List<Company> list = companyMapper.selectByMap(map);

	}
	
	@Test
	public void fourTest() {
        Map<String, Object> map = new HashMap<>();
		map.put("status", 1);

		List<Company> list = companyMapper.selectByMap(map);

	}

}
