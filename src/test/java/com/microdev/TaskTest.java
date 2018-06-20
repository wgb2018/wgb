package com.microdev;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;



import com.microdev.mapper.MessageMapper;
import com.microdev.mapper.TaskWorkerMapper;
import com.microdev.mapper.WorkerLogMapper;
import com.microdev.model.Message;
import com.microdev.model.TaskWorker;
import com.microdev.model.WorkLog;
import com.microdev.param.*;
import com.microdev.service.*;
import net.sf.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.TaskMapper;
import com.microdev.model.Task;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskTest {

	@Autowired
	private TaskService taskService;
	@Autowired
    private TaskMapper  taskMapper;
	@Autowired
	private MessageMapper messageMapper;
	@Autowired
	private WorkerLogMapper workerLogMapper;
	@Autowired
	private WorkerService workerService;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TaskWorkerService taskWorkerService;
	@Autowired
	private TaskWorkerMapper taskWorkerMapper;
	@Autowired
	private TaskHrCompanyService taskHrCompanyService;
	
	@Test
	public void oneTest() {
		String id = "dea60a58122440e38ef71a4c9027329b";
		String applyType = "worker";
		Map<String, Integer> map = messageService.selectUnReadCount(id, applyType);
		System.out.println(map);
	}


	@Test
	public void twoest() {
		String messageId = "a3b753f4820f4993bed2a5b216159857";
		String status = "1";
		workerService.workerHandleHrPay(messageId, status);
	}

	@Test
	public void threeTest() {
		UserTaskResponse response = workerService.selectUserTaskInfo("4211e6889658424d838c11d59500e99f", "dea60a58122440e38ef71a4c9027329b");
		JSONObject json = JSONObject.fromObject(response);
		System.out.println(json.toString());
	}

	@Test
	public void sixTest() {
		String messageId = "2e0db17253c34c5fa5d85a1106d25012";
		String status = "1";
		taskHrCompanyService.hrHandleIncome(messageId, status);
	}
}
