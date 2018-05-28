package com.microdev;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
