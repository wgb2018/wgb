package com.microdev;

import java.util.*;

import com.microdev.param.HrTaskDistributeRequest;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.TaskHrCompanyMapper;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.TaskHrQueryDTO;
import com.microdev.service.TaskHrCompanyService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskHrCompanyTest {

	@Autowired
	private TaskHrCompanyMapper taskHrCompanyMapper;
	@Autowired
	private TaskHrCompanyService taskHrCompanyService;
	
	@Test
	public void oneTest() {
		Map<String, Object> map = new HashMap<>();
		List<TaskHrCompany> list = taskHrCompanyMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			ResultDO r = taskHrCompanyService.getTaskHrCompanyById(id);
			System.out.println(r.toString());
		}
	}
	
	@Test
	public void twoTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(5);
		TaskHrQueryDTO taskHrQueryDTO = new TaskHrQueryDTO();
		ResultDO r = taskHrCompanyService.getPageTasks(paginator, taskHrQueryDTO);
		System.out.println(r.toString());
	}

	@Test
	public void threeTest() {
		HrTaskDistributeRequest hrTaskDis = new HrTaskDistributeRequest();
		hrTaskDis.setId("0a8054c4-408c-4090-b6e3-357c31c4196f");
		hrTaskDis.setHourlyPay(22d);
		Set<String> set = new HashSet<>();
		set.add("6642fb92-4e89-4d12-828a-8f42f9223b9b");
		set.add("4c2a2f69-1008-4c23-aa6d-e9c493e8ce31");
		set.add("50f7f6bd-5f78-48da-bddd-800ca3c30154");
		hrTaskDis.setWorkerIds(set);
		ResultDO r = taskHrCompanyService.TaskHrDistribute(hrTaskDis);
		JSONObject json = JSONObject.fromObject(r);
		if (r != null) {
			System.out.println(json);
		}
	}

	/*@Test
	public void fourTest() {
		String id = "0012c7dd-bb27-4475-b197-c2e870275653";
		String messageId = "dd750ed6dc444b16a4eb7369bdb3f6bc";
		taskHrCompanyService.TaskHrrefuse(id, messageId, "123");
	}

	@Test
	public void fiveTest() {
		String id = "0012c7dd-bb27-4475-b197-c2e870275653";
		String messageId = "3e8960615d704ec8acd0f94facc5e67f";
		taskHrCompanyService.TaskHraccept(id, messageId);
	}*/
}
