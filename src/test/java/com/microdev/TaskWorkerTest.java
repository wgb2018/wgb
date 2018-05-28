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
import com.microdev.mapper.TaskWorkerMapper;
import com.microdev.model.TaskWorker;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.service.TaskWorkerService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskWorkerTest {

	@Autowired
	private TaskWorkerService taskWorkerService;
	@Autowired
	private TaskWorkerMapper taskWorkerMapper;
	
	@Test
	public void oneTest() {
		Map<String, Object> map = new HashMap<>();
		List<TaskWorker> list = taskWorkerMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			ResultDO r = taskWorkerService.findWorkTaskById(id);
			if (r != null) {
				System.out.println(r.toString());
			}
		}
	} 
	
	@Test
	public void twoTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(6);
		TaskWorkerQuery queryDTO = new TaskWorkerQuery();
		ResultDO r = taskWorkerService.pagesTaskWorkers(paginator, queryDTO);
		if (r != null) {
			System.out.println(r.toString());
		}
	}
}
