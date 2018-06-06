package com.microdev;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microdev.mapper.DictMapper;
import com.microdev.mapper.MessageMapper;
import com.microdev.model.Message;
import com.microdev.param.DictDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.pagehelper.PageInfo;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.SocialMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Social;
import com.microdev.model.User;
import com.microdev.param.TokenDTO;
import com.microdev.param.UserDTO;
import com.microdev.param.UserTaskResponse;
import com.microdev.service.UserService;
import com.microdev.service.WorkerService;
import com.microdev.type.PlatformType;
import com.microdev.type.SocialType;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTest {

	@Autowired
    private UserMapper userMapper;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageMapper messageMapper;
	@Autowired
	private DictMapper dictMapper;
	
	@Test
	public void oneTest() throws Exception {
		User user = userMapper.queryByUserId("f904cd23-9fde-421d-a0f3-d6bd6cfad7c2");
		if (user != null) {
			UserDTO u = new UserDTO();
			u.setId(user.getPid());
			u.setPassword("123");
			u.setUsername(user.getUsername());
			u.setPlatform(PlatformType.PC);
			TokenDTO t = userService.login(u);
			System.out.println(t.toString());
		}

	}
	
	@Test
	public void threeTest() {
		Map<String, Object> map = new HashMap<>();
		List<User> list = userMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String userId = list.get(0).getPid();
			User u = userService.selectById(userId);
			if (u != null) {
				System.out.println(u.toString());
			}
		}
	}
	
	@Test
	public void sixTest() {
		Message message = messageMapper.selectById("a0d16ca48fd94fbbbf4b6b842b071735");
		OffsetDateTime nowTime = OffsetDateTime.now();
		OffsetDateTime applyTime = message.getCreateTime();
		long leaveMinute = nowTime.getLong(ChronoField.MINUTE_OF_DAY) - applyTime.getLong(ChronoField.MINUTE_OF_DAY);
		int hour = (int)(leaveMinute % 60 == 0 ? leaveMinute / 60 : (leaveMinute / 60) + 1);

		DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","22");
		Integer maxNum = Integer.parseInt(dict.getText());
		hour = maxNum * 24 - hour <= 0 ? 0 : maxNum * 24 - hour;
		String s = hour/24 + "天" + hour%24 + "小时";
		System.out.println(s);
	}

}
