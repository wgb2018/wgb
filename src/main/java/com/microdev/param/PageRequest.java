package com.microdev.param;

/**
 * 分页请求参数.
 * @author zhanglin
 *
 * @date 2018年5月10日
 */
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
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
