package com.microdev;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jayway.jsonpath.Criteria;
import com.microdev.param.CompanyQueryDTO;
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
	public void twoTest() {
        Map<String, Object> map = new HashMap<>();
		List<Company> list = companyMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			ResultDO r = companyService.getCompanyById(id);
			System.out.println(r.toString());
		}
	}
	
	@Test
	public void threeTest() {
        Map<String, Object> map = new HashMap<>();
		map.put("status", 0);

		List<Company> list = companyMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			//ResultDO r = companyService.hotelHrCompanies(id);
			//System.out.println(r.toString());
		}
	}
	
	@Test
	public void fourTest() {
        Map<String, Object> map = new HashMap<>();
		map.put("status", 1);

		List<Company> list = companyMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			/*ResultDO r = companyService.hotelHrCompanies(id);
			System.out.println(r.toString());*/
		}
	}
	
	@Test
	public void fiveTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("companyType", 1);

		List<Company>  list = companyMapper.selectByMap(map);
		if (list != null) {
			Company c = list.get(0);
			Map<String, Object> m = companyService.accountDetail(c.getPid(), 1,10);
			System.out.println(m.toString());
		}
	}
	
	@Test
	public void sixTest() {
		String id = "f5116454-e75d-489c-b108-36dab6027306";
		String status = "1";
		companyService.workExpand(id, status);
	}
	
	@Test
	public void sevenTest() {
		HotelDeployInfoRequest request = new HotelDeployInfoRequest();
		request.setTaskId("16103e7d-0a34-4cc6-8ee2-d512f40dfb7e");
		request.setTaskContent("测试酒店任务发布");
		request.setName("测试酒店");
		request.setHotelId("9ee67eb1-1178-42c1-a2fe-a3efe82c7632");
		request.setFromDate(OffsetDateTime.now().plusHours(-5));
		request.setToDate(OffsetDateTime.now().plusDays(7).plusHours(3));
		request.setHourlyPay(123.56);
		Map<String, Object> m = null;
		Set<Map<String, Object>> hrCompany = new HashSet<>();
		m = new HashMap<>();
		m.put("id", "b9bd3f40-fb01-45e0-9f7a-fe25784a144c");
		m.put("number", 3);
		hrCompany.add(m);
		m = new HashMap<>();
		m.put("id", "c955120f-2a17-4905-a0d9-b0edb5003086");
		m.put("number", 5);
		hrCompany.add(m);
		request.setHrCompany(hrCompany);
		companyService.hotelPublish(request);
	}
}
