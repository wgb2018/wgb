package com.microdev;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private WorkerService workerService;
	@Autowired
	private SocialMapper socialMapper;
	
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
		String taskWorkerId = "018f3e28-0526-44dd-8600-3dd8d5ade53c";
		String userId = "455877e5-d103-4f0e-a7be-c3413edd015b";
		UserTaskResponse response = workerService.selectUserTaskInfo(taskWorkerId, userId);
		System.out.println(response.toString());
	}
	/*@Test
	public void test() throws NoSuchAlgorithmException, InvalidKeySpecException {
		String str = PasswordHash.createHash("123");
		System.out.println(str);
	}*/
}
