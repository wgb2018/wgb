package com.microdev;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;



import com.microdev.mapper.MessageMapper;
import com.microdev.mapper.WorkerLogMapper;
import com.microdev.model.Message;
import com.microdev.model.WorkLog;
import com.microdev.param.*;
import com.microdev.service.InformService;
import com.microdev.service.WorkerService;
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
	@Autowired
	private WorkerLogMapper workerLogMapper;
	@Autowired
	private WorkerService workerService;
	@Autowired
	private InformService informService;

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

		WorkLog workLog = workerLogMapper.selectById("f291beb1a22b42cfa983f43b35e9b78f");
		OffsetDateTime time = OffsetDateTime.now();
		OffsetDateTime off = workLog.getModifyTime();
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		System.out.println(f.format(off));
		OffsetDateTime second = OffsetDateTime.ofInstant(Instant.ofEpochMilli(off.toEpochSecond()*1000), ZoneId.systemDefault());
		System.out.println("second:" + f.format(second));
	}

	@Test
	public void threeTest() {
		UserTaskResponse response = workerService.selectUserTaskInfo("b229cd5ab09d4d56bde740b86b7f1a16", "0638d16d6a0a42709f12c5b3ad5d901d");
		JSONObject json = JSONObject.fromObject(response);
		System.out.println(json.toString());
	}

	@Test
	public void fourTest() {
		InformRequestDTO dto = new InformRequestDTO();
		dto.setRole("hr");
		dto.setType(1);
		dto.setId("b1bad47abc574d8cb288e9871bfbab01");
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(6);
		Map<String, Object> map = informService.selectMessageInfo(dto, paginator);
		JSONObject json = JSONObject.fromObject(map);
		System.out.println(json.toString());
	}
}
