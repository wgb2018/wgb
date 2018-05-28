package com.microdev.common.paging;

import java.util.List;

/**
 * 支持分页的结果集，内含总条目数，当前页码，每页条目数等信息
 * @author liutf
 */
public class PagedList<T> {
    /**
     * 是不是首页
     */
    private boolean first;
    /**
     * 是不是末页
     */
    private boolean last;
    /**
     * 总页数
     */
    private int totalPage;
    /**
     * 信息总数
     */
    private long totalCount;
    /**
     * 当前页数
     */
    private int currentPage;
    /**
     * 每页的信息条数
     */
    private int pageSize;
    /**
     * 信息列表
     */
    private List<T> list;

    public PagedList() {

    }

    public PagedList(long totalCount, int currentPage, int pageSize, List<T> list) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.list = list;
    }

    public PagedList(long totalCount, Paginator paginator, List<T> list) {
        this.totalCount = totalCount;
        this.currentPage = paginator.getPage();
        this.pageSize = paginator.getPageSize();
        this.list = list;
    }

    public static <E> PagedList<E> wrap(List<E> list) {
        return new PagedList<E>(list.size(), Paginator.page(1, list.size()), list);
    }


    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
