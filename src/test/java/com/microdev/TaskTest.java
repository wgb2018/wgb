package com.microdev;

import java.time.OffsetDateTime;
import java.util.*;

import com.microdev.param.CreateTaskRequest;
import com.microdev.param.TaskHrCompanyDTO;
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

	@Test
	public void threeTest() {
		CreateTaskRequest request = new CreateTaskRequest();
		request.setHotelId("9ee67eb1-1178-42c1-a2fe-a3efe82c7632");
		Set<TaskHrCompanyDTO> hrCompanySet= new HashSet<TaskHrCompanyDTO>();
		TaskHrCompanyDTO dto = new TaskHrCompanyDTO();
		dto.setHrCompanyId("5ae8b896-49e1-4fbc-9328-97f06355dbee");
		dto.setNeedWorkers(4);
		hrCompanySet.add(dto);
		dto = new TaskHrCompanyDTO();
		dto.setHrCompanyId("64af9145-ec74-433e-8943-dbeca6f2fc3d");
		dto.setNeedWorkers(5);
		hrCompanySet.add(dto);
		dto = new TaskHrCompanyDTO();
		dto.setHrCompanyId("69871259-fc02-439e-9844-c8a7d44273b2");
		dto.setNeedWorkers(6);
		hrCompanySet.add(dto);
		request.setTaskTypeText("测试服务");
		request.setTaskTypeCode("test01");
		request.setTaskContent("酒店发布任务测试");
		request.setHourlyPay(35);
		request.setHrCompanySet(hrCompanySet);
		request.setFromDate(OffsetDateTime.now());
		request.setToDate(OffsetDateTime.now().plusDays(10));
		taskService.createTask(request);
	}
}
