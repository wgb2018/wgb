package com.microdev;

import java.time.OffsetDateTime;
import java.util.*;

import com.microdev.common.WorkerUnbind;
import com.microdev.mapper.MessageMapper;
import com.microdev.model.Message;
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
	@Autowired
	private MessageMapper messageMapper;
	
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
	public void twoest() {
		Message m = messageMapper.selectById("b34921ea57de410b8e0b28072dc001ad");
		System.out.println(m.toString());
		/*m.setStatus(0);
		messageMapper.updateById(m);
		System.out.println(m.getStatus());*/
	}
}
