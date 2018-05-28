package com.microdev;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.param.HrQueryWorkerDTO;
import com.microdev.param.WokerQueryHrDTO;
import com.microdev.service.UserCompanyService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserCompanyTest {

	@Autowired
	private UserCompanyService userCompanyService;
	
	@Test
	public void oneTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(6);
		HrQueryWorkerDTO queryDTO = new HrQueryWorkerDTO();
		queryDTO.setHrId("275f6074-2c2c-4488-b870-2e683613c057");
		ResultDO r = userCompanyService.getHrWorkers(paginator, queryDTO);
		if (r != null) {
			System.out.println(r.toString());
		}
	}
	
	@Test
	public void twoTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(6);
		WokerQueryHrDTO queryDTO = new WokerQueryHrDTO();
		queryDTO.setWorkerId("1a3acc0d-4cf6-4c11-b8ce-f8e0210a72fc");
		ResultDO r = userCompanyService.getWorkerHrs(paginator, queryDTO);
		if (r != null) {
			System.out.println(r.toString());
		}
	}

	@Test
	public void threeTest() {
		String hrId = "275f6074-2c2c-4488-b870-2e683613c057";
		String workerId = "51d454a6-b1e9-4a4e-8a6d-86993c24034d";
		userCompanyService.workerUnbindHr(workerId, hrId);
	}

	@Test
	public void fourTest() {
		String hrId = "275f6074-2c2c-4488-b870-2e683613c057";
		String workerId = "51d454a6-b1e9-4a4e-8a6d-86993c24034d";
		String messageId = "04f84c55-1476-4fcf-baf1-23ad17c76df5";
		userCompanyService.workerBindHr(workerId, hrId, messageId);
	}
}
