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
import com.microdev.mapper.TaskMapper;
import com.microdev.model.Task;
import com.microdev.param.TaskQueryDTO;
import com.microdev.service.TaskService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskTest {

	@Autowired
	private TaskService taskService;
	@Autowired
    private TaskMapper  taskMapper;
	
	@Test
	public void oneTest() {
		Map<String, Object> map = new HashMap<>();
		List<Task> list = taskMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
			ResultDO r = taskService.getTaskById(id);
			System.out.println(r.toString());
		}
	}
	
	@Test
	public void twoTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(6);
		TaskQueryDTO taskQueryDTO = new TaskQueryDTO();
		ResultDO r = taskService.getPageTasks(paginator, taskQueryDTO);
		System.out.println(r.toString());
	}
}
