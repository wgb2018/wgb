package com.microdev.common;

import com.microdev.common.paging.Paginator;
import lombok.Data;

/**
 * @author liutf
 */
@Data
public class PagingDO<T> {
    private Paginator paginator = new Paginator();
    private T selector;

//    private Paginator paged = new Paginator();
//    private T query;
}
