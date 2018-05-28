package com.microdev.common.paging;

import java.util.List;

/**
 * @author liutf
 */
public class Paginator {
    /**
     * 默认条目数
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /**
     * 默认第一页的页码
     */
    public static final int DEFAULT_FIRST_PAGE = 1;
    /**
     * 每页显示多少条记录
     */
    private int pageSize = Paginator.DEFAULT_PAGE_SIZE;
    /**
     * 当前页码
     */
    private int page = Paginator.DEFAULT_FIRST_PAGE;

    /**
     * 排序
     */
    private List<Sort> sort;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize < 1 ? 1 : pageSize;
    }

    public List<Sort> getSort() {
        return sort;
    }

    public void setSort(List<Sort> sort) {
        this.sort = sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int offset() {
        return (page - 1) * pageSize;
    }

    public static Paginator page(int page) {
        Paginator paginator = new Paginator();
        paginator.setPage(page);
        return paginator;
    }

    public static Paginator page(int page, int pageSize) {
        Paginator paginator = new Paginator();
        paginator.setPage(page);
        paginator.setPageSize(pageSize);
        return paginator;
    }
}
