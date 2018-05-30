package com.microdev.param;

import lombok.Data;

/**
 * 分页请求参数.
 * @author zhanglin
 *
 * @date 2018年5月10日
 */
@Data
public class PageRequest {

	/**
	 * 页码
	 */
	private Integer page;
	/**
	 * 每页页数
	 */
	private Integer pageSize;
	/**
	 * workerId
	 */
	private String id;

	private int role;

	private int type;
	
}
