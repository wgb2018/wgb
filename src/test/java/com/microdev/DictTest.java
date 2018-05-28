package com.microdev;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microdev.param.DictDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.RedisUtil;
import com.microdev.mapper.DictMapper;
import com.microdev.model.Dict;
import com.microdev.service.DictService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DictTest {

	@Autowired
	private DictService dictService;
	@Autowired
    private DictMapper dictMapper;
	@Autowired
	private RedisUtil redisUtil;
	
	@Test
	public void oneTest() {
		Paginator paginator = new Paginator();
		paginator.setPage(1);
		paginator.setPageSize(8);
        DictDTO dictDTO = new DictDTO();
        ResultDO page = dictService.paging(paginator, dictDTO);
		System.out.println(page.toString());
	}
	
	@Test
	public void twoTest() {
		String name = "TaskType";
		ResultDO page = dictService.findByName(name);
		System.out.println(page.toString());
	}
	
	@Test
	public void threeTest() {
        Map<String, Object> map = new HashMap<>();
		List<Dict> list = dictMapper.selectByMap(map);
		if (list != null && list.size() > 0) {
			String id = list.get(0).getPid();
            DictDTO page = dictService.getById(id);
			System.out.println(page.toString());
		}
		
	}
	
	@Test
	public void fourTest() {
        Map<String, Object> map = new HashMap<>();
        List<DictDTO> list = dictService.list();
		if (list != null) {
			System.out.println(list.toString());
		}
	}
}
